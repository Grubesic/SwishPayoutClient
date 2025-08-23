
package com.rogr.swishpayoutclient.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScaffold(
    sidebar: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit
) {
    // Background gradient like screenshot
    val bg = Brush.radialGradient(
        colors = listOf(Color(0xFF0E1420), Color(0xFF0A0E16), Color(0xFF080B12)),
        center = androidx.compose.ui.geometry.Offset.Zero,
        radius = 1200f
    )
    Row(Modifier.fillMaxSize().background(bg)) {
        // Sidebar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.width(240.dp).fillMaxHeight()
        ) {
            Column(Modifier.padding(20.dp), content = sidebar)
        }

        // Content card, glassy feel
        Box(
            Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth(0.78f).wrapContentHeight()
            ) {
                Box(Modifier.padding(24.dp)) { content() }
            }
        }
    }
}
