package com.rogr.swishpayoutclient.service

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

    suspend fun postPayout(req: PayoutRequest): PayoutResponse {
        //val json = Json.encodeToString(req)
        val response = client.post(baseUrl) {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        val body = response.bodyAsText()
        return PayoutResponse(response.status.isSuccess(), response.status.value, body)
    }
}