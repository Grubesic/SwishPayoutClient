package com.rogr.swishpayoutclient.ui


import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun Sidebar(onSelect: (String)->Unit, current: String) {
    Text("Nordbil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
    Spacer(Modifier.height(24.dp))

    @Composable
    fun Item(label: String, id: String) {
        NavigationDrawerItem(
            label = { Text(label) },
            selected = current == id,
            onClick = { onSelect(id) },
            modifier = Modifier.fillMaxWidth()
        )
    }

    Item("Betalning", "newpayout")
    Item("Inställningar", "settings")
    /*Item("Betalningar", "payments")
    Spacer(Modifier.height(16.dp))
    Text("Användare", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
    Item("Användare", "users")
    Item("Inställningar", "settings")
    Spacer(Modifier.height(16.dp))
    Item("Ny utbetalning", "newpayout") */
}