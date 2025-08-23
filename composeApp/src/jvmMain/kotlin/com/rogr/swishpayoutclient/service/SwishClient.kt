package com.rogr.swishpayoutclient.service

import com.rogr.swishpayoutclient.core.models.CreatePayoutResult
import com.rogr.swishpayoutclient.core.models.PayoutRequest
import com.rogr.swishpayoutclient.core.models.PayoutResponse
import io.ktor.client.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import javax.net.ssl.SSLContext
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.delay

class SwishClient(private val baseUrl: String, private val sslContext: SSLContext) {
    val logFile: Path = Path.of(System.getProperty("user.home"), "swish-client.log")

    private val client = HttpClient(Java) {
        engine {
            config {
                sslContext(sslContext)
            }
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    Files.writeString(
                        logFile,
                        message + "\n",
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND,
                        StandardOpenOption.WRITE
                    )
                }
            }
            level = LogLevel.ALL
        }
        install(ContentNegotiation) {
            json(Json {
                encodeDefaults = true
                explicitNulls = false
                prettyPrint = false
            })
        }

    }

   /* suspend fun postPayout(req: PayoutRequest): PayoutResponse {
        //val json = Json.encodeToString(req)
        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        val body = response.bodyAsText()
        return PayoutResponse(response.status.isSuccess(), response.status.value, body)
    }*/

    suspend fun postPayout(req: PayoutRequest): CreatePayoutResult {
        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        val body = response.bodyAsText()
        val loc = response.headers[HttpHeaders.Location]
        return CreatePayoutResult(response.status.isSuccess(), response.status.value, body, loc)
    }

    suspend fun getPayout(payoutInstructionUUID: String): PayoutResponse {
        val url = "${baseUrl.trimEnd('/')}/${payoutInstructionUUID}"
        val response = client.get(url) { accept(ContentType.Application.Json) }
        val body = response.bodyAsText()
        return PayoutResponse(response.status.isSuccess(), response.status.value, body)
    }


    suspend fun pollPayoutUntilDone(
        uuid: String,
        maxAttempts: Int = 20,              // ~ 20 * 3s = 60s
        intervalMs: Long = 3000
    ): PayoutResponse {
        repeat(maxAttempts) { attempt ->
            val res = getPayout(uuid)
            // naive check: look for status in body; you can parse with kotlinx.serialization if you add a model
            if (res.ok && ("\"status\":\"PAID\"" in res.body || "\"status\":\"ERROR\"" in res.body)) {
                return res
            }
            delay(intervalMs)
        }
        return getPayout(uuid) // final read
    }

}