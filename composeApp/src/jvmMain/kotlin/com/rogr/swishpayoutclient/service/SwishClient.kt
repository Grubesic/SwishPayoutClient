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
import java.io.PrintStream

class SwishClient(private val baseUrl: String, private val sslContext: SSLContext) {
    val logFile: Path = Path.of(System.getProperty("user.home"), "swish-client.log")

    companion object {
        init {
            System.setProperty("javax.net.debug", "ssl,handshake,record,verbose,certpath")
            System.err.println("javax.net.debug=" + System.getProperty("javax.net.debug"))
            System.setProperty("jdk.tls.client.protocols", "TLSv1.2")
        }
    }

    init {
        val pid = try { java.lang.ProcessHandle.current().pid() } catch (_: Throwable) { -1 }
        // 1) Prove init ran & where we're writing
        java.nio.file.Files.writeString(
            logFile,
            "=== SwishClient init (pid=$pid) writing to ${logFile}\n",
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.APPEND,
            java.nio.file.StandardOpenOption.WRITE
        )

        val ps = PrintStream(
            Files.newOutputStream(
                logFile,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE
            ),
            /* autoFlush = */ true,
            /* charset    = */ Charsets.UTF_8.name()
        )
        System.setErr(ps)
        System.setOut(ps)
        System.err.println(">>> redirected stderr")
        System.out.println(">>> redirected stdout")

        val s = (javax.net.ssl.SSLSocketFactory.getDefault()
            .createSocket("example.com", 443) as javax.net.ssl.SSLSocket)
        s.startHandshake()
        s.close()
        System.out.println(("javax.net.debug=" + System.getProperty("javax.net.debug")))
        System.out.println(("Running on Java " + System.getProperty("java.version")))


    }

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