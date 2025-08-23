package com.rogr.swishpayoutclient.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF8EE0FF),
    onPrimary = Color(0xFF001018),
    surface = Color(0xFF0F1218),
    onSurface = Color(0xFFE6EAEE),
    surfaceVariant = Color(0xFF171B22),
    outline = Color(0xFF2B313B),
)

val CardShape = RoundedCornerShape(20.dp)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        shapes = Shapes(
            small = CardShape, medium = CardShape, large = CardShape
        ),
        typography = Typography(),
        content = content
    )
}