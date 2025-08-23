package com.rogr.swishpayoutclient.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PaymentRow(val amount: String, val receiver: String, val status: String)

@Composable
fun PaymentsTable(rows: List<PaymentRow>) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text("Belopp", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Text("Mottagare", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            Text("Status", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
        LazyColumn(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(rows) { r ->
                Row(Modifier.fillMaxWidth()) {
                    Text(r.amount, Modifier.weight(1f))
                    Text(r.receiver, Modifier.weight(1f))
                    AssistChip(onClick = {}, label = { Text(r.status) })
                }
                HorizontalDivider(Modifier, DividerDefaults.Thickness, DividerDefaults.color)
            }
        }
        Text("Visar 1 till ${rows.size} av ${rows.size} resultat", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    }
}