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
    val plugins by viewModel.plugins.collectAsState()
    val customRepos by viewModel.customRepos.collectAsState()
    val terminalLines by viewModel.terminalLines.collectAsState()
    val isExecuting by viewModel.isExecuting.collectAsState()

    var connectionConfig by remember { mutableStateOf(ConnectionConfig()) }
    var showCommandsDialog by remember { mutableStateOf<com.claude.codecompanion.data.MCPPlugin?>(null) }

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
                    icon = { Icon(Icons.Default.Terminal, contentDescription = null) },
                    label = { Text("Terminal") },
                    selected = currentRoute == "terminal",
                    onClick = {
                        navController.navigate("terminal") {
                            popUpTo("dashboard")
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Extension, contentDescription = null) },
                    label = { Text("Plugins") },
                    selected = currentRoute == "plugins",
                    onClick = {
                        navController.navigate("plugins") {
                            popUpTo("dashboard")
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    label = { Text("Settings") },
                    selected = currentRoute == "settings",
                    onClick = {
                        navController.navigate("settings") {
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

            composable("terminal") {
                TerminalScreen(
                    terminalLines = terminalLines,
                    isExecuting = isExecuting,
                    onExecuteCommand = { command ->
                        viewModel.executeTerminalCommand(command)
                    },
                    onClearTerminal = {
                        viewModel.clearTerminal()
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

            composable("plugins") {
                PluginMarketplaceScreen(
                    plugins = plugins,
                    customRepos = customRepos,
                    onInstallPlugin = { plugin ->
                        viewModel.installPlugin(plugin)
                    },
                    onUninstallPlugin = { plugin ->
                        viewModel.uninstallPlugin(plugin)
                    },
                    onAddRepository = { name, url ->
                        viewModel.addCustomRepository(name, url)
                    },
                    onViewCommands = { plugin ->
                        showCommandsDialog = plugin
                    }
                )
            }

            composable("settings") {
                SettingsScreen(
                    connectionConfig = connectionConfig,
                    onSaveConfig = { config ->
                        connectionConfig = config
                    },
                    onConnect = {
                        viewModel.connect(connectionConfig)
                    }
                )
            }
        }
    }

    // Show plugin commands dialog
    showCommandsDialog?.let { plugin ->
        PluginCommandsDialog(
            plugin = plugin,
            onDismiss = { showCommandsDialog = null }
        )
    }
}
