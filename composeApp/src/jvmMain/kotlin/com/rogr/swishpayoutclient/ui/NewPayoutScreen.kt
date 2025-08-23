package com.rogr.swishpayoutclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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

    var live by remember { mutableStateOf<PayoutLiveStatus?>(null) }

    var isSigValidBeforeSend by remember { mutableStateOf<Boolean?>(null) }

    var isSubmitting by remember { mutableStateOf(false) }


    // Center + max width
    Box(
        modifier = modifier
            .padding(insets)
            .fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 720.dp)            // 👈 cap width
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // --- Two-column form ---
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = amount, onValueChange = { amount = it },
                        label = { Text("Belopp (##.##)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = payeeAlias, onValueChange = { payeeAlias = it },
                        label = { Text("Mottagare (telefon)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = payeeSSN, onValueChange = { payeeSSN = it },
                        label = { Text("Mottagare Personnummer") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = message, onValueChange = { message = it },
                        label = { Text("Meddelande") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = payerPaymentReference,
                        onValueChange = { payerPaymentReference = it },
                        label = { Text("Betalar-referens") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            HorizontalDivider()

            // Passwords side-by-side
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PasswordField(
                    value = tlsPass,
                    onValueChange = { tlsPass = it },
                    label = "TLS-lösenord",
                    modifier = Modifier.weight(1f)
                )
                PasswordField(
                    value = signingPass,
                    onValueChange = { signingPass = it },
                    label = "Signerings-lösenord",
                    modifier = Modifier.weight(1f)
                )
            }

            // Actions
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        isSubmitting = true

                        scope.launch(Dispatchers.IO) {
                            try {
                                // show initial row
                                live = PayoutLiveStatus(
                                    uuid = "…", // temp, replaced below
                                    amount = "${amount} kr",
                                    receiver = payeeAlias,
                                    status = "CREATING",
                                    loading = true
                                )

                                val resText = runCatching {
                                    val signing = loadSigningMaterial(
                                        settings.signingP12Path,
                                        signingPass.toCharArray()
                                    )
                                    val ssl =
                                        buildSslContext(settings.tlsP12Path, tlsPass.toCharArray())
                                    val client = SwishClient(settings.baseUrl, ssl)

                                    val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()
                                    live = live?.copy(uuid = uuid)

                                    val payload = PayoutPayload(
                                        payoutInstructionUUID = uuid,
                                        payerPaymentReference = payerPaymentReference,
                                        payerAlias = settings.payerAlias,
                                        payeeAlias = payeeAlias,
                                        payeeSSN = payeeSSN,
                                        amount = requireAmount(amount),
                                        currency = "SEK",
                                        payoutType = "PAYOUT",
                                        message = message.ifBlank { null },
                                        instructionDate = DateUtils.getInstructionDate(),
                                        signingCertificateSerialNumber = signing.serialHex
                                    )

                                    val canonical = canonicalFromPayload(payload, Json {
                                        encodeDefaults = true; explicitNulls = false; prettyPrint =
                                        false
                                    })
                                    val signature = signSwish(canonical, signing.privateKey)
                                    isSigValidBeforeSend =
                                        verifySwishSignature(canonical, signature, signing.publicKey)

                                    val req = PayoutRequest(
                                        payload = payload,
                                        callbackUrl = settings.callbackUrl.ifBlank { null },
                                        callbackIdentifier = uuid,
                                        signature = signature
                                    )

                                    // CREATE
                                    val create = client.postPayout(req)
                                    if (create.status == 201) {
                                        live =
                                            live?.copy(status = "CREATED", loading = true, note = null)
                                    } else {
                                        live = live?.copy(
                                            status = "ERROR",
                                            loading = false,
                                            note = "Skapa misslyckades (${create.status})"
                                        )
                                        return@runCatching "create: ${create.status}"
                                    }

                                    // POLL
                                    live = live?.copy(
                                        status = "POLLING",
                                        loading = true,
                                        note = "Väntar på status…"
                                    )
                                    val finalRes = client.pollPayoutUntilDone(uuid)
                                    val body = finalRes.body

                                    val status = when {
                                        body.contains("\"status\":\"PAID\"") -> "PAID"
                                        body.contains("\"status\":\"DEBITED\"") -> "DEBITED"
                                        body.contains("\"status\":\"ERROR\"") -> "ERROR"
                                        else -> "CREATED"
                                    }
                                    live = live?.copy(
                                        status = status,
                                        loading = false,
                                        note = if (status == "ERROR") "Fel vid utbetalning" else null
                                    )

                                    "OK"
                                }.getOrElse {
                                    live =
                                        live?.copy(status = "ERROR", loading = false, note = it.message)
                                    "Error"
                                }

                                result = resText

                            } finally {
                                signingPass = ""
                                tlsPass = ""
                                isSubmitting = false
                            }

                        }

                    },
                    enabled = signingPass.isNotBlank() && tlsPass.isNotBlank() && !isSubmitting,
                    modifier = Modifier.weight(1f)
                ) { Text("Skicka utbetalning") }
            }

            // Optional previews (kept narrow & readable)
            if (previewPayload.isNotBlank()) {
                HorizontalDivider()
                Text(
                    "Canonical payload:",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(previewPayload, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "Signature (base64):",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(previewSig, color = MaterialTheme.colorScheme.onSurface)
            }

            // Live status row under the form, same max width
            live?.let {
                HorizontalDivider()
                Text("Status", color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                PayoutStatusRow(it)
            }

            isSigValidBeforeSend?.let {
                Text(
                    "Signatur lokal verifikation: ${if (it) "OK ✅" else "FEL ❌"}",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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