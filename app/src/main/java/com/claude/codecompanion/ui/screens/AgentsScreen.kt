package com.claude.codecompanion.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.claude.codecompanion.data.Agent
import com.claude.codecompanion.data.AgentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen(
    agents: List<Agent>,
    onAgentExecute: (Agent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Agents",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Quick access to Claude Code agents",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(agents) { agent ->
                AgentCard(
                    agent = agent,
                    onExecute = { onAgentExecute(agent) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentCard(
    agent: Agent,
    onExecute: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        onClick = { showDialog = true }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = getAgentIcon(agent.type),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Column {
                Text(
                    text = agent.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = agent.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }

    if (showDialog) {
        AgentExecutionDialog(
            agent = agent,
            onDismiss = { showDialog = false },
            onExecute = {
                onExecute()
                showDialog = false
            }
        )
    }
}

@Composable
fun AgentExecutionDialog(
    agent: Agent,
    onDismiss: () -> Unit,
    onExecute: () -> Unit
) {
    var prompt by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = getAgentIcon(agent.type),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(agent.name) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = agent.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    label = { Text("Task Description") },
                    placeholder = { Text("Enter task for this agent...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Usage Tips:",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = getAgentTips(agent.type),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onExecute,
                enabled = prompt.isNotBlank()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("Execute")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun getAgentIcon(type: AgentType) = when (type) {
    AgentType.EXPLORE -> Icons.Default.Search
    AgentType.PLAN -> Icons.Default.List
    AgentType.GENERAL_PURPOSE -> Icons.Default.Build
    AgentType.CODE_REVIEWER -> Icons.Default.Code
    AgentType.TEST_RUNNER -> Icons.Default.PlayArrow
    AgentType.CUSTOM -> Icons.Default.Star
}

fun getAgentTips(type: AgentType) = when (type) {
    AgentType.EXPLORE -> "Great for finding files, searching code, and understanding codebase structure"
    AgentType.PLAN -> "Perfect for breaking down complex features into implementation steps"
    AgentType.GENERAL_PURPOSE -> "Use for multi-step tasks requiring various tools and operations"
    AgentType.CODE_REVIEWER -> "Reviews code for bugs, security issues, and improvements"
    AgentType.TEST_RUNNER -> "Executes tests and reports results"
    AgentType.CUSTOM -> "Custom agent with specific capabilities"
}
