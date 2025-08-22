package com.rogr.swishpayoutclient.util

import java.awt.FileDialog
import java.awt.Frame

fun chooseFile(): String? {
    val dialog = FileDialog(null as Frame?, "Select PKCS#12", FileDialog.LOAD)
    dialog.isVisible = true
    val dir = dialog.directory ?: return null
    val file = dialog.file ?: return null
    return "$dir$file"
}