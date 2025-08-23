package com.rogr.swishpayoutclient

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = " \uD83D\uDCB6 Aston Martin \uD83D\uDCB6 Swish Payout \uD83D\uDCB6",
        state = WindowState(width = 1280.dp, height = 900.dp)
    ) {
        println("Running on Java " + System.getProperty("java.version"))
        App()
    }
}