package com.rogr.swishpayoutclient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.rogr.swishpayoutclient.ui.NewPayoutScreen
import com.rogr.swishpayoutclient.ui.SettingsScreen
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import swishpayoutclient.composeapp.generated.resources.Res
import swishpayoutclient.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    MaterialTheme {
        var tab by remember { mutableStateOf(0) }
        Scaffold(
            topBar = {
                TabRow(selectedTabIndex = tab) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("New Payout") })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Settings") })
                }
            }
        ) { padding ->
            when (tab) {
                0 -> NewPayoutScreen(Modifier.fillMaxSize(), padding)
                1 -> SettingsScreen(Modifier.fillMaxSize(), padding)
            }
        }
    }
}