package com.rogr.swishpayoutclient.shared

// commonMain

enum class YkTransport { USB, NFC, BLE, LIGHTNING, UNKNOWN }
enum class YkCapability { PIV, FIDO2, OATH, OTP, OPENPGP }

data class YkBasicInfo(
    val serial: Long?,
    val version: String?,
    val formFactor: String?,   // nano, keychain, etc
    val transports: Set<YkTransport>,
    val capabilities: Set<YkCapability>
)

sealed interface YkEvent {
    data class Connected(val info: YkBasicInfo): YkEvent
    data object Disconnected : YkEvent
    data class Error(val message: String): YkEvent
}

interface YubiKeyService {
    /** Hot-plug stream of device presence & info. Emits latest on subscription. */
    val events: kotlinx.coroutines.flow.StateFlow<YkEvent>

    /** True while a device is present. */
    val isPresent: kotlinx.coroutines.flow.StateFlow<Boolean>

    /** Optional: get a SmartCard (PIV) handle/session when present. */
    suspend fun <R> withPivSession(block: suspend (YkPivSession) -> R): R
}

/** Keep PIV concerns abstract so platforms can wrap their session objects. */
interface YkPivSession {
    suspend fun getAppletVersion(): String?
    // add more when needed: sign, list certs, read CHUID, etc.
}