package com.rogr.swishpayoutclient

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = " \uD83D\uDCB6 NordbilPay \uD83D\uDCB6",
        state = WindowState(width = 1280.dp, height = 900.dp),
    ) {
        println("Running on Java " + System.getProperty("java.version"))
        App()
    }
}