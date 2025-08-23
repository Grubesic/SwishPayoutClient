package com.rogr.swishpayoutclient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import com.rogr.swishpayoutclient.ui.AppTheme
import com.rogr.swishpayoutclient.ui.DashboardScaffold
import com.rogr.swishpayoutclient.ui.NewPayoutScreen
import com.rogr.swishpayoutclient.ui.PaymentRow
import com.rogr.swishpayoutclient.ui.PaymentsTable
import com.rogr.swishpayoutclient.ui.SettingsScreen
import com.rogr.swishpayoutclient.ui.Sidebar
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import swishpayoutclient.composeapp.generated.resources.Res
import swishpayoutclient.composeapp.generated.resources.compose_multiplatform

@Composable
@Preview
fun App() {
    AppTheme {
        var tab by remember { mutableStateOf("newpayout") } // default to your screen

        DashboardScaffold(
            sidebar = { Sidebar(onSelect = { tab = it }, current = tab) }
        ) {
            when (tab) {
                "newpayout" -> NewPayoutScreen(Modifier, PaddingValues())
                "payments"  -> PaymentsTable(
                    rows = listOf(
                        PaymentRow("1 920,00 kr", "46730351824", "Skapad"),
                        PaymentRow("500,00 kr","46730220026","Skapad"),
                        PaymentRow("222,00 kr","46730322116","Skapad"),
                    )
                )
                "dashboard" -> Text("Dashboard – coming soon")
                "certs"     -> Text("Certifikat – coming soon")
                "users"     -> Text("Användare – coming soon")
                "settings"   -> SettingsScreen(Modifier, PaddingValues())
                else        -> Text("…")
            }
        }
    }
}