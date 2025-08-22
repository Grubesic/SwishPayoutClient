package com.rogr.swishpayoutclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rogr.swishpayoutclient.core.*
import com.rogr.swishpayoutclient.core.models.*
import com.rogr.swishpayoutclient.service.SwishClient
import com.rogr.swishpayoutclient.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.UUID

@Composable
fun NewPayoutScreen(modifier: Modifier, insets: PaddingValues) {
    val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf(AppSettings.load()) }

    var amount by remember { mutableStateOf("100.00") }
    var payeeAlias by remember { mutableStateOf("0728648607") }
    var payeeSSN by remember { mutableStateOf("197709306828") }
    var message by remember { mutableStateOf("Message to the recipient.") }
    var payerPaymentReference by remember { mutableStateOf("payerRef") }

    var tlsPass by remember { mutableStateOf("") }
    var signingPass by remember { mutableStateOf("") }

    var previewPayload by remember { mutableStateOf("") }
    var previewSig by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }

    var isSigValidBeforeSend by remember { mutableStateOf<Boolean?>(null) }

    Column(modifier.padding(insets).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(amount, { amount = it }, label = { Text("Amount (##.##)") })
        OutlinedTextField(payeeAlias, { payeeAlias = it }, label = { Text("Payee Alias (46...)") })
        OutlinedTextField(payeeSSN, { payeeSSN = it }, label = { Text("Payee SSN (optional)") })
        OutlinedTextField(message, { message = it }, label = { Text("Message") })
        OutlinedTextField(payerPaymentReference, { payerPaymentReference = it }, label = { Text("Payer Payment Reference") })

        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        OutlinedTextField(tlsPass, { tlsPass = it }, label = { Text("TLS password") })
        OutlinedTextField(signingPass, { signingPass = it }, label = { Text("Signing password") })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                runCatching {
                    val signing = loadSigningMaterial(settings.signingP12Path, signingPass.toCharArray())
                    val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()

                    val payload = PayoutPayload(
                        payoutInstructionUUID = uuid,
                        payerPaymentReference = payerPaymentReference,
                        payerAlias = settings.payerAlias,
                        //payeeAlias = normalizeAlias(payeeAlias),
                        payeeAlias = payeeAlias,
                        payeeSSN = payeeSSN.ifBlank { null },
                        amount = requireAmount(amount),
                        currency = "SEK",
                        payoutType = "PAYOUT",
                        message = message.ifBlank { null },
                        instructionDate = DateUtils.getInstructionDate(),
                        signingCertificateSerialNumber = signing.serialHex
                    )
                    val canonical = canonicalFromPayload(payload, Json {
                        encodeDefaults = true
                        explicitNulls = false
                        prettyPrint = false
                    })
                    val sig = signSha512withRsa(canonical.toByteArray(Charsets.UTF_8), signing.privateKey)

                    previewPayload = canonical
                    previewSig = sig
                }.onFailure { previewPayload = "Error: ${it.message}" }
            }) { Text("Preview Sign") }

            Button(onClick = {
                scope.launch(Dispatchers.IO) {
                    result = "Sending..."
                    val res = runCatching {
                        val signing = loadSigningMaterial(settings.signingP12Path, signingPass.toCharArray())
                        val ssl = buildSslContext(settings.tlsP12Path, tlsPass.toCharArray())
                        val client = SwishClient(settings.baseUrl, ssl)

                        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()
                        val payload = PayoutPayload(
                            payoutInstructionUUID = uuid,
                            payerPaymentReference = payerPaymentReference,
                            payerAlias = settings.payerAlias,
                            //payeeAlias = normalizeAlias(payeeAlias),
                            payeeAlias = payeeAlias,
                            payeeSSN = payeeSSN.ifBlank { null },
                            amount = requireAmount(amount),
                            currency = "SEK",
                            payoutType = "PAYOUT",
                            message = message.ifBlank { null },
                            instructionDate = DateUtils.getInstructionDate(),
                            signingCertificateSerialNumber = signing.serialHex
                        )

                        val canonical = canonicalFromPayload(payload, Json {
                            encodeDefaults = true
                            explicitNulls = false
                            prettyPrint = false
                        })

                       val signature = signSwish(canonical, signing.privateKey)
                        isSigValidBeforeSend = verifySwishSignature(canonical, signature, signing.publicKey)


                        val req = PayoutRequest(
                            payload = payload,
                            callbackUrl = settings.callbackUrl.ifBlank { null },
                            callbackIdentifier = uuid,
                            signature = signature
                        )
                        client.postPayout(req)
                    }.fold(
                        onSuccess = { it },
                        onFailure = { PayoutResponse(false, -1, "Error: ${it.message}") }
                    )
                    result = "ok=${res.ok}, status=${res.statusCode}\n${res.body}"
                }
            }) { Text("Send Payout") }
        }

        if (previewPayload.isNotBlank()) {
            Text("Canonical payload:\n$previewPayload")
            Text("Signature (base64):\n$previewSig")
        }
        if (result.isNotBlank()) {
            HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            Text("Result:\n$result")
        }
        isSigValidBeforeSend?.let {
            Text("Signature valid (local): ${if (it) "YES ✅" else "NO ❌"}")
        }
    }
}

private fun normalizeAlias(input: String): String {
    val digits = input.filter(Char::isDigit)
    return if (digits.startsWith("46")) digits else if (digits.startsWith("0")) "46${digits.drop(1)}" else "46$digits"
}

private fun requireAmount(s: String): String {
    require(Regex("^\\d+\\.\\d{2}$").matches(s)) { "Amount must be like 100.00" }
    return s
}