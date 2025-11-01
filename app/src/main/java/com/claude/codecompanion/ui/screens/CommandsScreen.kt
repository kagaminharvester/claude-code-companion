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
import com.claude.codecompanion.data.Command
import com.claude.codecompanion.data.CommandCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommandsScreen(
    commands: List<Command>,
    commandOutput: String,
    onExecuteCommand: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<CommandCategory?>(null) }
    var showOutputDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Commands",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search commands...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All") }
            )
            FilterChip(
                selected = selectedCategory == CommandCategory.GIT,
                onClick = { selectedCategory = CommandCategory.GIT },
                label = { Text("Git") }
            )
            FilterChip(
                selected = selectedCategory == CommandCategory.BUILD,
                onClick = { selectedCategory = CommandCategory.BUILD },
                label = { Text("Build") }
            )
            FilterChip(
                selected = selectedCategory == CommandCategory.TEST,
                onClick = { selectedCategory = CommandCategory.TEST },
                label = { Text("Test") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Commands List
        val filteredCommands = commands.filter { command ->
            (searchQuery.isEmpty() || command.name.contains(searchQuery, ignoreCase = true) ||
                    command.command.contains(searchQuery, ignoreCase = true)) &&
                    (selectedCategory == null || command.category == selectedCategory)
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Favorites Section
            val favorites = filteredCommands.filter { it.isFavorite }
            if (favorites.isNotEmpty()) {
                item {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                items(favorites) { command ->
                    CommandCard(
                        command = command,
                        onExecute = {
                            onExecuteCommand(command.command)
                            showOutputDialog = true
                        },
                        onToggleFavorite = { onToggleFavorite(command.id) }
                    )
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // All Commands
            val nonFavorites = filteredCommands.filter { !it.isFavorite }
            items(nonFavorites) { command ->
                CommandCard(
                    command = command,
                    onExecute = {
                        onExecuteCommand(command.command)
                        showOutputDialog = true
                    },
                    onToggleFavorite = { onToggleFavorite(command.id) }
                )
            }

            if (filteredCommands.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No commands found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showOutputDialog) {
        CommandOutputDialog(
            output = commandOutput,
            onDismiss = { showOutputDialog = false }
        )
    }
}

@Composable
fun CommandCard(
    command: Command,
    onExecute: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = command.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = command.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row {
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (command.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (command.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.small
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = command.command,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onExecute,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Run")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = command.category.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CommandOutputDialog(
    output: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Command Output") },
        text = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = output.ifEmpty { "Executing command..." },
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
