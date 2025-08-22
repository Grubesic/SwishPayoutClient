package com.rogr.swishpayoutclient.core


import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@Serializable
data class AppSettings(
    val environment: String = "Test",
    val baseUrl: String = "https://<swish-test-host>/payouts",
    val tlsP12Path: String = "",
    val signingP12Path: String = "",
    val callbackUrl: String = "https://example.com/callback",
    val payerAlias: String = "",
    val defaultCurrency: String = "SEK"
) {
    companion object {
        private val json = Json { prettyPrint = true; encodeDefaults = true }
        private val file: Path = Path.of(System.getProperty("user.home"), ".swish-payout-settings.json")

        fun load(): AppSettings = runCatching {
            if (!Files.exists(file)) return AppSettings()
            json.decodeFromString(serializer<AppSettings>(),Files.readString(file))
        }.getOrElse { AppSettings() }

        fun save(settings: AppSettings) {
            Files.writeString(
                file,
                json.encodeToString(serializer(), settings),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE
            )
        }
    }
}