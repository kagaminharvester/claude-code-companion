package com.claude.codecompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.claude.codecompanion.data.ConnectionConfig
import com.claude.codecompanion.ui.screens.*
import com.claude.codecompanion.ui.theme.ClaudeCodeCompanionTheme
import com.claude.codecompanion.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClaudeCodeCompanionTheme {
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val tasks by viewModel.tasks.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val agents by viewModel.agents.collectAsState()
    val commands by viewModel.commands.collectAsState()
    val workflows by viewModel.workflows.collectAsState()
    val commandOutput by viewModel.commandOutput.collectAsState()

    var connectionConfig by remember { mutableStateOf(ConnectionConfig()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Claude Code Companion") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text("Dashboard") },
                    selected = currentRoute == "dashboard",
                    onClick = {
                        navController.navigate("dashboard") {
                            popUpTo("dashboard") { inclusive = true }
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Tasks") },
                    selected = currentRoute == "tasks",
                    onClick = {
                        navController.navigate("tasks") {
                            popUpTo("dashboard")
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Build, contentDescription = null) },
                    label = { Text("Agents") },
                    selected = currentRoute == "agents",
                    onClick = {
                        navController.navigate("agents") {
                            popUpTo("dashboard")
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Code, contentDescription = null) },
                    label = { Text("Commands") },
                    selected = currentRoute == "commands",
                    onClick = {
                        navController.navigate("commands") {
                            popUpTo("dashboard")
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                    label = { Text("Workflows") },
                    selected = currentRoute == "workflows",
                    onClick = {
                        navController.navigate("workflows") {
                            popUpTo("dashboard")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    tasks = tasks,
                    connectionState = connectionState,
                    onConnect = { viewModel.connect(connectionConfig) },
                    onDisconnect = { viewModel.disconnect() },
                    onTaskStatusChange = { taskId, status ->
                        viewModel.updateTaskStatus(taskId, status)
                    }
                )
            }

            composable("tasks") {
                TasksScreen(
                    tasks = tasks,
                    onAddTask = { content, activeForm ->
                        viewModel.addTask(content, activeForm)
                    },
                    onUpdateTaskStatus = { taskId, status ->
                        viewModel.updateTaskStatus(taskId, status)
                    },
                    onDeleteTask = { taskId ->
                        viewModel.deleteTask(taskId)
                    }
                )
            }

            composable("agents") {
                AgentsScreen(
                    agents = agents,
                    onAgentExecute = { agent ->
                        // Execute agent command via SSH
                        viewModel.executeCommand("claude task ${agent.type.name.lowercase()}")
                    }
                )
            }

            composable("commands") {
                CommandsScreen(
                    commands = commands,
                    commandOutput = commandOutput,
                    onExecuteCommand = { command ->
                        viewModel.executeCommand(command)
                    },
                    onToggleFavorite = { commandId ->
                        viewModel.toggleCommandFavorite(commandId)
                    }
                )
            }

            composable("workflows") {
                WorkflowsScreen(
                    workflows = workflows,
                    onExecuteWorkflow = { workflow ->
                        viewModel.executeWorkflow(workflow)
                    }
                )
            }
        }
    }

    // Settings FAB (Floating Action Button) overlay
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    if (currentRoute == "dashboard") {
        var showSettings by remember { mutableStateOf(false) }

        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                title = { Text("Connection Settings") },
                text = {
                    SettingsScreen(
                        connectionConfig = connectionConfig,
                        onSaveConfig = { config ->
                            connectionConfig = config
                            showSettings = false
                        },
                        onConnect = {
                            viewModel.connect(connectionConfig)
                        }
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showSettings = false }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
