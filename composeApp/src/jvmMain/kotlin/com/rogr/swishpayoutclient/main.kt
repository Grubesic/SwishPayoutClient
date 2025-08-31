package com.rogr.swishpayoutclient

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    // (A) Turn on JSSE debug BEFORE touching SSLContext/HttpClient/Ktor/etc.
    System.setProperty("javax.net.debug", "ssl,handshake,record,verbose,certpath")
    System.setProperty("jdk.tls.client.protocols", "TLSv1.2")

    // (B) Redirect both streams (optional but useful)
    val log = java.nio.file.Path.of(System.getProperty("user.home"), "swish-client.log")
    val ps = java.io.PrintStream(
        java.nio.file.Files.newOutputStream(
            log,
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.APPEND,
            java.nio.file.StandardOpenOption.WRITE
        ),
        true,
        Charsets.UTF_8.name()
    )
    System.setErr(ps)
    System.setOut(ps)
    System.out.println("TLS debug ON (pre-init)")
    Window(
        onCloseRequest = ::exitApplication,
        title = " \uD83D\uDCB6 NordbilPay \uD83D\uDCB6",
        state = WindowState(width = 1280.dp, height = 900.dp),
    ) {
        App()
    }
}