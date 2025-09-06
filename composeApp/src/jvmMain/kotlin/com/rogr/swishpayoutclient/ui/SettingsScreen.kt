package com.rogr.swishpayoutclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rogr.swishpayoutclient.core.AppSettings
import com.rogr.swishpayoutclient.core.buildSslContext
import com.rogr.swishpayoutclient.util.chooseFile
import javax.net.ssl.SSLContext

@Composable
fun SettingsScreen(modifier: Modifier, insets: PaddingValues) {
    var settings by remember { mutableStateOf(AppSettings.load()) }
    var tlsPass by remember { mutableStateOf("") }
    var mTlsOk by remember { mutableStateOf<Boolean?>(null) }

    Column(modifier.padding(insets).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(settings.environment, { settings = settings.copy(environment = it) }, label = { Text("Environment") })
        OutlinedTextField(settings.baseUrl, { settings = settings.copy(baseUrl = it) }, label = { Text("Base URL") })



        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = settings.trustPemChainPath,
                onValueChange = { settings = settings.copy(trustPemChainPath = it) },
                label = { Text("Trust PEM chain (optional)") },
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { chooseFile()?.let { settings = settings.copy(trustPemChainPath = it) } }) { Text("Browse") }
        }

        OutlinedTextField(settings.callbackUrl, { settings = settings.copy(callbackUrl = it) }, label = { Text("Callback URL") })
        OutlinedTextField(settings.payerAlias, { settings = settings.copy(payerAlias = it) }, label = { Text("Payer Alias") })

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { AppSettings.save(settings) }) { Text("Save") }
            Button(onClick = {
                mTlsOk = runCatching {
                    val ctx: SSLContext = buildSslContext(settings.tlsP12Path, tlsPass.toCharArray())
                    ctx != null
                }.getOrDefault(false)
            }) { Text("Test mTLS") }
        }

        mTlsOk?.let { Text(if (it) "mTLS OK ✅" else "mTLS failed ❌", color = if (it) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error) }
    }
}