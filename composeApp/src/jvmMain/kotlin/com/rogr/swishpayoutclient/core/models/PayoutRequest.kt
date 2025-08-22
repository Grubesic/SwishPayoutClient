package com.rogr.swishpayoutclient.core.models

import kotlinx.serialization.Serializable

@Serializable
data class PayoutRequest(
    val payload: PayoutPayload,
    val callbackUrl: String?,
    val callbackIdentifier: String,
    val signature: String
)

data class PayoutResponse(val ok: Boolean, val statusCode: Int, val body: String)