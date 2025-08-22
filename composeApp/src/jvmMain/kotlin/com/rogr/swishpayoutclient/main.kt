package com.rogr.swishpayoutclient

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = " \uD83D\uDCB6 Aston Martin \uD83D\uDCB6 Swish Payout \uD83D\uDCB6",
    ) {
        println("Running on Java " + System.getProperty("java.version"))
        App()
    }
}