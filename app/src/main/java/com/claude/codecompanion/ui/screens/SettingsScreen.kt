package com.claude.codecompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.claude.codecompanion.data.ConnectionConfig

@Composable
fun SettingsScreen(
    connectionConfig: ConnectionConfig,
    onSaveConfig: (ConnectionConfig) -> Unit,
    onConnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var host by remember { mutableStateOf(connectionConfig.host) }
    var port by remember { mutableStateOf(connectionConfig.port.toString()) }
    var username by remember { mutableStateOf(connectionConfig.username) }
    var password by remember { mutableStateOf(connectionConfig.password) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SSH Connection",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host") },
            placeholder = { Text("192.168.0.51") },
            leadingIcon = { Icon(Icons.Default.Cloud, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Port") },
            placeholder = { Text("22") },
            leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            placeholder = { Text("pi") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            placeholder = { Text("Enter password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val config = ConnectionConfig(
                    host = host,
                    port = port.toIntOrNull() ?: 22,
                    username = username,
                    password = password
                )
                onSaveConfig(config)
                onConnect()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = host.isNotBlank() && username.isNotBlank() && password.isNotBlank()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Save & Connect")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Divider()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Claude Code Companion v1.0",
            style = MaterialTheme.typography.bodyMedium
        )

        Text(
            text = "Enhance your Claude Code workflow with task management, agent quick access, and command execution",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
