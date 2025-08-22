package com.rogr.swishpayoutclient.core


import com.rogr.swishpayoutclient.core.models.PayoutPayload
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.util.Base64


fun signSwish(payloadJson: String, privateKey: PrivateKey): String {
    val bytes = payloadJson.toByteArray(Charsets.UTF_8)

    // Step 1: SHA-512 hash of the payload JSON
    val hash = MessageDigest.getInstance("SHA-512").digest(bytes)

    // Step 2: sign that hash using SHA512withRSA
    val sig = Signature.getInstance("SHA512withRSA")
    sig.initSign(privateKey)
    sig.update(hash)
    val signature = sig.sign()

    return Base64.getEncoder().encodeToString(signature)
}

fun verifySwishSignature(payloadJson: String, signatureB64: String, certPublicKey: java.security.PublicKey): Boolean {
    val hash = MessageDigest.getInstance("SHA-512")
        .digest(payloadJson.toByteArray(Charsets.UTF_8))
    val sig = Signature.getInstance("SHA512withRSA")
    sig.initVerify(certPublicKey)
    sig.update(hash)
    return sig.verify(Base64.getDecoder().decode(signatureB64))
}

fun canonicalFromPayload(payload: PayoutPayload, json: Json): String {
    return json.encodeToString(payload)
}