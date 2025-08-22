package com.rogr.swishpayoutclient.core.models

import kotlinx.serialization.Serializable

@Serializable
data class PayoutPayload(
    val payoutInstructionUUID: String,
    val payerPaymentReference: String,
    val payerAlias: String,
    val payeeAlias: String,
    val payeeSSN: String? = null,
    val amount: String,
    val currency: String = "SEK",
    val payoutType: String = "PAYOUT",
    val message: String? = null,
    val instructionDate: String,
    val signingCertificateSerialNumber: String
)