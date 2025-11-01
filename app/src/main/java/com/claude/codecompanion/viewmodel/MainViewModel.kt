package com.claude.codecompanion.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.claude.codecompanion.data.*
import com.claude.codecompanion.ssh.SSHManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val sshManager = SSHManager()

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _agents = MutableStateFlow(getDefaultAgents())
    val agents: StateFlow<List<Agent>> = _agents.asStateFlow()

    private val _commands = MutableStateFlow(getDefaultCommands())
    val commands: StateFlow<List<Command>> = _commands.asStateFlow()

    private val _workflows = MutableStateFlow(getDefaultWorkflows())
    val workflows: StateFlow<List<Workflow>> = _workflows.asStateFlow()

    private val _commandOutput = MutableStateFlow("")
    val commandOutput: StateFlow<String> = _commandOutput.asStateFlow()

    fun connect(config: ConnectionConfig) {
        viewModelScope.launch {
            val result = sshManager.connect(config)
            _connectionState.value = if (result.isSuccess) {
                ConnectionState(
                    isConnected = true,
                    message = result.getOrNull() ?: "Connected",
                    lastConnected = System.currentTimeMillis()
                )
            } else {
                ConnectionState(
                    isConnected = false,
                    message = result.exceptionOrNull()?.message ?: "Connection failed"
                )
            }
        }
    }

    fun disconnect() {
        sshManager.disconnect()
        _connectionState.value = ConnectionState(
            isConnected = false,
            message = "Disconnected"
        )
    }

    fun executeCommand(command: String) {
        viewModelScope.launch {
            val result = sshManager.executeCommand(command)
            _commandOutput.value = result.getOrNull() ?: result.exceptionOrNull()?.message ?: "Error"
        }
    }

    fun addTask(content: String, activeForm: String) {
        val newTask = Task(
            id = System.currentTimeMillis().toString(),
            content = content,
            status = TaskStatus.PENDING,
            activeForm = activeForm
        )
        _tasks.value = _tasks.value + newTask
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(status = status)
            } else {
                task
            }
        }
    }

    fun deleteTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }

    fun executeWorkflow(workflow: Workflow) {
        viewModelScope.launch {
            workflow.steps.sortedBy { it.order }.forEach { step ->
                executeCommand(step.action)
            }
        }
    }

    fun toggleCommandFavorite(commandId: String) {
        _commands.value = _commands.value.map { command ->
            if (command.id == commandId) {
                command.copy(isFavorite = !command.isFavorite)
            } else {
                command
            }
        }
    }

    private fun getDefaultAgents(): List<Agent> {
        return listOf(
            Agent(
                "1",
                "Explore",
                "Fast agent specialized for exploring codebases",
                AgentType.EXPLORE,
                "search"
            ),
            Agent(
                "2",
                "Plan",
                "Create implementation plans for complex tasks",
                AgentType.PLAN,
                "list"
            ),
            Agent(
                "3",
                "General Purpose",
                "Multi-step autonomous task handling",
                AgentType.GENERAL_PURPOSE,
                "build"
            ),
            Agent(
                "4",
                "Code Reviewer",
                "Review code for issues and improvements",
                AgentType.CODE_REVIEWER,
                "code"
            )
        )
    }

    private fun getDefaultCommands(): List<Command> {
        return listOf(
            // Claude Code Commands
            Command("claude1", "Start Claude Session", "cd \$CLAUDE_DIR && claude", "Start interactive Claude Code session", CommandCategory.CLAUDE, true),
            Command("claude2", "Claude Task", "cd \$CLAUDE_DIR && claude task 'your task here'", "Execute a Claude task", CommandCategory.CLAUDE, true),
            Command("claude3", "Claude MCP List", "cd \$CLAUDE_DIR && claude mcp list", "List MCP servers", CommandCategory.CLAUDE),
            Command("claude4", "Claude Status", "cd \$CLAUDE_DIR && ps aux | grep claude", "Check if Claude is running", CommandCategory.CLAUDE),
            Command("claude5", "Claude Version", "claude --version", "Check Claude Code version", CommandCategory.CLAUDE),

            // Git Commands
            Command("1", "Git Status", "git status", "Check repository status", CommandCategory.GIT, true),
            Command("2", "Git Log", "git log --oneline -10", "View recent commits", CommandCategory.GIT),
            Command("7", "Git Diff", "git diff", "View changes", CommandCategory.GIT),
            Command("8", "Pull", "git pull", "Pull latest changes", CommandCategory.GIT),

            // File Operations
            Command("3", "List Files", "ls -la", "List all files", CommandCategory.FILE_OPERATIONS),
            Command("4", "Search Code", "grep -r 'pattern' .", "Search in codebase", CommandCategory.SEARCH),

            // Build & Test
            Command("5", "Build", "npm run build", "Build the project", CommandCategory.BUILD),
            Command("6", "Test", "npm test", "Run tests", CommandCategory.TEST)
        )
    }

    private fun getDefaultWorkflows(): List<Workflow> {
        return listOf(
            Workflow(
                "0",
                "Claude Code Session",
                "Start Claude Code in working directory",
                listOf(
                    WorkflowStep(1, "cd \$CLAUDE_DIR", emptyMap()),
                    WorkflowStep(2, "claude --version", emptyMap()),
                    WorkflowStep(3, "claude mcp list", emptyMap())
                )
            ),
            Workflow(
                "1",
                "Commit & Push",
                "Stage, commit, and push changes",
                listOf(
                    WorkflowStep(1, "git add .", emptyMap()),
                    WorkflowStep(2, "git commit -m 'Update'", emptyMap()),
                    WorkflowStep(3, "git push", emptyMap())
                )
            ),
            Workflow(
                "2",
                "Build & Test",
                "Build and run tests",
                listOf(
                    WorkflowStep(1, "npm install", emptyMap()),
                    WorkflowStep(2, "npm run build", emptyMap()),
                    WorkflowStep(3, "npm test", emptyMap())
                )
            ),
            Workflow(
                "3",
                "Pull & Install",
                "Pull latest and install dependencies",
                listOf(
                    WorkflowStep(1, "git pull", emptyMap()),
                    WorkflowStep(2, "npm install", emptyMap())
                )
            )
        )
    }
}
