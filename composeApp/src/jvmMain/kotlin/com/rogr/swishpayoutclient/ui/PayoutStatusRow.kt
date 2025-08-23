package com.rogr.swishpayoutclient.ui


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PayoutLiveStatus(
    val uuid: String,
    val amount: String,
    val receiver: String,
    val status: String,         // CREATED / DEBITED / PAID / ERROR / CREATING / POLLING
    val note: String? = null,
    val loading: Boolean = false
)

@Composable
fun PayoutStatusRow(s: PayoutLiveStatus) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text("Belopp", color = MaterialTheme.colorScheme.onSurface)
            Text(s.amount, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Column(Modifier.weight(1f)) {
            Text("Mottagare", color = MaterialTheme.colorScheme.onSurface)
            Text(s.receiver, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
        Column(Modifier.weight(1f)) {
            Text("Status", color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {},
                    label = { Text(statusLabel(s.status)) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = when (s.status) {
                            "PAID" -> MaterialTheme.colorScheme.primary.copy(0.20f)
                            "DEBITED" -> MaterialTheme.colorScheme.primary.copy(0.12f)
                            "ERROR" -> MaterialTheme.colorScheme.error.copy(0.20f)
                            "CREATED","CREATING","POLLING" -> MaterialTheme.colorScheme.surfaceVariant
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                )
                if (s.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                }
            }
            s.note?.let { Text(it, color = MaterialTheme.colorScheme.onSurface) }
        }
    }
}

private fun statusLabel(s: String) = when (s) {
    "CREATING" -> "Skapar…"
    "CREATED"  -> "Skapad"
    "POLLING"  -> "Verifierar…"
    "DEBITED"  -> "Debiterad"
    "PAID"     -> "Betald"
    "ERROR"    -> "Fel"
    else       -> s
}