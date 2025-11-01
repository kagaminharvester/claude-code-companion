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
import com.claude.codecompanion.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginMarketplaceScreen(
    plugins: List<MCPPlugin>,
    customRepos: List<CustomRepository>,
    onInstallPlugin: (MCPPlugin) -> Unit,
    onUninstallPlugin: (MCPPlugin) -> Unit,
    onAddRepository: (String, String) -> Unit,
    onViewCommands: (MCPPlugin) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddRepoDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<PluginCategory?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredPlugins = plugins.filter { plugin ->
        (selectedCategory == null || plugin.category == selectedCategory) &&
        (searchQuery.isEmpty() ||
         plugin.name.contains(searchQuery, ignoreCase = true) ||
         plugin.description.contains(searchQuery, ignoreCase = true))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plugin Marketplace",
                style = MaterialTheme.typography.headlineMedium
            )

            IconButton(onClick = { showAddRepoDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Repository")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search plugins") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Filter Chips
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                    PluginCategory.values().forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category.name.replace("_", " ")) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Plugin Cards
            items(filteredPlugins) { plugin ->
                PluginCard(
                    plugin = plugin,
                    onInstall = { onInstallPlugin(plugin) },
                    onUninstall = { onUninstallPlugin(plugin) },
                    onViewCommands = { onViewCommands(plugin) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Custom Repositories Section
            if (customRepos.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Custom Repositories",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(customRepos) { repo ->
                    CustomRepoCard(repo = repo)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddRepoDialog) {
        AddRepositoryDialog(
            onDismiss = { showAddRepoDialog = false },
            onAdd = { name, url ->
                onAddRepository(name, url)
                showAddRepoDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginCard(
    plugin: MCPPlugin,
    onInstall: () -> Unit,
    onUninstall: () -> Unit,
    onViewCommands: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = plugin.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = {},
                    label = { Text(plugin.category.name.replace("_", " ")) },
                    leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = plugin.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Repository: ${plugin.repository}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (plugin.commands.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onViewCommands) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("${plugin.commands.size} commands available")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (plugin.isInstalled) {
                    Button(
                        onClick = onUninstall,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Uninstall")
                    }
                } else {
                    Button(onClick = onInstall) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text("Install")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomRepoCard(repo: CustomRepository) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = repo.name,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = repo.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddRepositoryDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var repoName by remember { mutableStateOf("") }
    var repoUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Repository") },
        text = {
            Column {
                OutlinedTextField(
                    value = repoName,
                    onValueChange = { repoName = it },
                    label = { Text("Repository Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repoUrl,
                    onValueChange = { repoUrl = it },
                    label = { Text("Repository URL") },
                    placeholder = { Text("https://github.com/user/repo") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(repoName, repoUrl) },
                enabled = repoName.isNotBlank() && repoUrl.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PluginCommandsDialog(
    plugin: MCPPlugin,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${plugin.name} Commands") },
        text = {
            LazyColumn {
                items(plugin.commands) { command ->
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = command.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = command.description,
                            style = MaterialTheme.typography.bodySmall
                        )
                        if (command.parameters.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Parameters:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            command.parameters.forEach { param ->
                                Text(
                                    text = "  • ${param.name} (${param.type})${if (param.required) " *" else ""} - ${param.description}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
