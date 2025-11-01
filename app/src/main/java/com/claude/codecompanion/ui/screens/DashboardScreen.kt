package com.claude.codecompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.claude.codecompanion.data.ConnectionState
import com.claude.codecompanion.data.Task
import com.claude.codecompanion.data.TaskStatus

@Composable
fun DashboardScreen(
    tasks: List<Task>,
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTaskStatusChange: (String, TaskStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Connection Status Card
        ConnectionStatusCard(
            connectionState = connectionState,
            onConnect = onConnect,
            onDisconnect = onDisconnect
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats
        QuickStatsCard(tasks = tasks)

        Spacer(modifier = Modifier.height(16.dp))

        // Tasks Overview
        Text(
            text = "Recent Tasks",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tasks.takeLast(10).reversed()) { task ->
                TaskCard(
                    task = task,
                    onStatusChange = { newStatus ->
                        onTaskStatusChange(task.id, newStatus)
                    }
                )
            }

            if (tasks.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(
    connectionState: ConnectionState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (connectionState.isConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (connectionState.isConnected) "Connected" else "Disconnected",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = connectionState.message,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Button(
                onClick = if (connectionState.isConnected) onDisconnect else onConnect
            ) {
                Icon(
                    imageVector = if (connectionState.isConnected)
                        Icons.Default.Close
                    else
                        Icons.Default.Check,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (connectionState.isConnected) "Disconnect" else "Connect")
            }
        }
    }
}

@Composable
fun QuickStatsCard(tasks: List<Task>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatCard(
            title = "Total",
            value = tasks.size.toString(),
            icon = Icons.Default.List,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "In Progress",
            value = tasks.count { it.status == TaskStatus.IN_PROGRESS }.toString(),
            icon = Icons.Default.PlayArrow,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Completed",
            value = tasks.count { it.status == TaskStatus.COMPLETED }.toString(),
            icon = Icons.Default.Done,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onStatusChange: (TaskStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.content,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = task.activeForm,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TaskStatusBadge(status = task.status)
        }
    }
}

@Composable
fun TaskStatusBadge(status: TaskStatus) {
    val (color, icon, text) = when (status) {
        TaskStatus.PENDING -> Triple(
            MaterialTheme.colorScheme.outline,
            Icons.Default.Info,
            "Pending"
        )
        TaskStatus.IN_PROGRESS -> Triple(
            MaterialTheme.colorScheme.primary,
            Icons.Default.PlayArrow,
            "In Progress"
        )
        TaskStatus.COMPLETED -> Triple(
            MaterialTheme.colorScheme.tertiary,
            Icons.Default.Done,
            "Done"
        )
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        contentColor = color,
        shape = MaterialTheme.shapes.small
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Connect to Claude Code to start tracking tasks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
