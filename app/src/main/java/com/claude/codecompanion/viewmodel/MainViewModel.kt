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

    private val _plugins = MutableStateFlow(getDefaultPlugins())
    val plugins: StateFlow<List<MCPPlugin>> = _plugins.asStateFlow()

    private val _customRepos = MutableStateFlow<List<CustomRepository>>(emptyList())
    val customRepos: StateFlow<List<CustomRepository>> = _customRepos.asStateFlow()

    private val _terminalLines = MutableStateFlow<List<TerminalLine>>(emptyList())
    val terminalLines: StateFlow<List<TerminalLine>> = _terminalLines.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

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

    fun executeTerminalCommand(command: String) {
        viewModelScope.launch {
            _isExecuting.value = true

            // Add command to terminal
            _terminalLines.value = _terminalLines.value + TerminalLine(
                text = command,
                type = LineType.COMMAND
            )

            // Execute command
            val result = sshManager.executeCommand(command)

            // Add output to terminal
            val output = result.getOrNull() ?: result.exceptionOrNull()?.message ?: "Error"
            val lineType = if (result.isSuccess) {
                LineType.OUTPUT
            } else {
                LineType.ERROR
            }

            _terminalLines.value = _terminalLines.value + TerminalLine(
                text = output,
                type = lineType
            )

            _isExecuting.value = false
        }
    }

    fun clearTerminal() {
        _terminalLines.value = emptyList()
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
            // Core Claude Code Commands
            Command("claude1", "Start Claude REPL", "cd \$CLAUDE_DIR && claude", "Start interactive Claude Code session", CommandCategory.CLAUDE, true),
            Command("claude2", "Query & Exit", "cd \$CLAUDE_DIR && claude -p \"your query here\"", "Query via SDK then exit", CommandCategory.CLAUDE, true),
            Command("claude3", "Resume Session", "cd \$CLAUDE_DIR && claude -c", "Resume most recent conversation", CommandCategory.CLAUDE),
            Command("claude4", "Continue with Query", "cd \$CLAUDE_DIR && claude -c -p \"your query\"", "Continue previous session with new query", CommandCategory.CLAUDE),
            Command("claude5", "Update Claude", "claude update", "Update Claude Code to latest version", CommandCategory.CLAUDE),

            // MCP Management
            Command("mcp1", "List MCP Servers", "claude mcp list", "Show all installed MCP servers", CommandCategory.CLAUDE),
            Command("mcp2", "Add MCP Server", "claude mcp add --transport stdio <package> -- npx -y <package>", "Install new MCP server", CommandCategory.CLAUDE),
            Command("mcp3", "Remove MCP Server", "claude mcp remove <package>", "Uninstall MCP server", CommandCategory.CLAUDE),

            // File Operations with Claude
            Command("file1", "Explain File", "cd \$CLAUDE_DIR && claude -p \"Explain @filepath\"", "Get explanation of specific file", CommandCategory.FILE_OPERATIONS),
            Command("file2", "Analyze Directory", "cd \$CLAUDE_DIR && claude -p \"Analyze @directory\"", "Get structure of directory", CommandCategory.FILE_OPERATIONS),

            // Code Analysis
            Command("code1", "Codebase Overview", "cd \$CLAUDE_DIR && claude -p \"Give me overview of this codebase\"", "Understand project structure", CommandCategory.SEARCH),
            Command("code2", "Find Functionality", "cd \$CLAUDE_DIR && claude -p \"Where is X functionality?\"", "Locate specific features", CommandCategory.SEARCH),
            Command("code3", "Trace Flow", "cd \$CLAUDE_DIR && claude -p \"Trace execution flow for X\"", "Understand code execution", CommandCategory.SEARCH)
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

    fun installPlugin(plugin: MCPPlugin) {
        viewModelScope.launch {
            val command = "claude mcp add --transport stdio ${plugin.packageName} -- npx -y ${plugin.packageName}"
            executeCommand(command)

            // Mark as installed
            _plugins.value = _plugins.value.map {
                if (it.id == plugin.id) it.copy(isInstalled = true) else it
            }
        }
    }

    fun uninstallPlugin(plugin: MCPPlugin) {
        viewModelScope.launch {
            val command = "claude mcp remove ${plugin.packageName}"
            executeCommand(command)

            // Mark as uninstalled
            _plugins.value = _plugins.value.map {
                if (it.id == plugin.id) it.copy(isInstalled = false) else it
            }
        }
    }

    fun addCustomRepository(name: String, url: String) {
        val repo = CustomRepository(
            id = System.currentTimeMillis().toString(),
            name = name,
            url = url
        )
        _customRepos.value = _customRepos.value + repo
    }

    private fun getDefaultPlugins(): List<MCPPlugin> {
        return listOf(
            MCPPlugin(
                id = "1",
                name = "Filesystem",
                description = "Read, write, and manage files and directories. Provides comprehensive file operations including reading, writing, editing, searching, and directory management.",
                packageName = "@modelcontextprotocol/server-filesystem",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.FILE_SYSTEM,
                commands = listOf(
                    PluginCommand("read_file", "Read complete file contents", listOf(
                        CommandParameter("path", "string", "Path to the file")
                    )),
                    PluginCommand("write_file", "Create or overwrite a file", listOf(
                        CommandParameter("path", "string", "Path to the file"),
                        CommandParameter("content", "string", "File content")
                    )),
                    PluginCommand("edit_file", "Make line-based edits", listOf(
                        CommandParameter("path", "string", "Path to the file"),
                        CommandParameter("edits", "array", "Array of edit operations")
                    )),
                    PluginCommand("list_directory", "List directory contents", listOf(
                        CommandParameter("path", "string", "Directory path")
                    )),
                    PluginCommand("search_files", "Search for files by pattern", listOf(
                        CommandParameter("path", "string", "Search root path"),
                        CommandParameter("pattern", "string", "Search pattern")
                    ))
                )
            ),
            MCPPlugin(
                id = "2",
                name = "GitHub",
                description = "Interact with GitHub repositories, issues, and pull requests. Create repos, manage files, search code, handle issues and PRs.",
                packageName = "@modelcontextprotocol/server-github",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.DEVELOPMENT,
                commands = listOf(
                    PluginCommand("create_repository", "Create a new GitHub repository", listOf(
                        CommandParameter("name", "string", "Repository name"),
                        CommandParameter("description", "string", "Repository description", false),
                        CommandParameter("private", "boolean", "Make repository private", false)
                    )),
                    PluginCommand("get_file_contents", "Get file contents from a repository", listOf(
                        CommandParameter("owner", "string", "Repository owner"),
                        CommandParameter("repo", "string", "Repository name"),
                        CommandParameter("path", "string", "File path")
                    )),
                    PluginCommand("create_issue", "Create a new issue", listOf(
                        CommandParameter("owner", "string", "Repository owner"),
                        CommandParameter("repo", "string", "Repository name"),
                        CommandParameter("title", "string", "Issue title"),
                        CommandParameter("body", "string", "Issue body", false)
                    )),
                    PluginCommand("create_pull_request", "Create a pull request", listOf(
                        CommandParameter("owner", "string", "Repository owner"),
                        CommandParameter("repo", "string", "Repository name"),
                        CommandParameter("title", "string", "PR title"),
                        CommandParameter("head", "string", "Head branch"),
                        CommandParameter("base", "string", "Base branch")
                    )),
                    PluginCommand("search_code", "Search code across repositories", listOf(
                        CommandParameter("q", "string", "Search query")
                    ))
                )
            ),
            MCPPlugin(
                id = "3",
                name = "Brave Search",
                description = "Web search using Brave Search API. Perform web searches, local searches, and get diverse search results.",
                packageName = "@modelcontextprotocol/server-brave-search",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.WEB,
                commands = listOf(
                    PluginCommand("brave_web_search", "Search the web", listOf(
                        CommandParameter("query", "string", "Search query"),
                        CommandParameter("count", "number", "Number of results", false)
                    )),
                    PluginCommand("brave_local_search", "Search for local businesses", listOf(
                        CommandParameter("query", "string", "Local search query")
                    ))
                )
            ),
            MCPPlugin(
                id = "4",
                name = "Puppeteer",
                description = "Browser automation using Puppeteer. Navigate websites, take screenshots, click elements, fill forms, and execute JavaScript.",
                packageName = "@modelcontextprotocol/server-puppeteer",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.AUTOMATION,
                commands = listOf(
                    PluginCommand("puppeteer_navigate", "Navigate to a URL", listOf(
                        CommandParameter("url", "string", "URL to navigate to")
                    )),
                    PluginCommand("puppeteer_screenshot", "Take a screenshot", listOf(
                        CommandParameter("name", "string", "Screenshot name"),
                        CommandParameter("selector", "string", "CSS selector", false)
                    )),
                    PluginCommand("puppeteer_click", "Click an element", listOf(
                        CommandParameter("selector", "string", "CSS selector")
                    )),
                    PluginCommand("puppeteer_fill", "Fill an input field", listOf(
                        CommandParameter("selector", "string", "CSS selector"),
                        CommandParameter("value", "string", "Value to fill")
                    )),
                    PluginCommand("puppeteer_evaluate", "Execute JavaScript", listOf(
                        CommandParameter("script", "string", "JavaScript code")
                    ))
                )
            ),
            MCPPlugin(
                id = "5",
                name = "PostgreSQL",
                description = "Interact with PostgreSQL databases. Execute queries, manage schemas, and perform database operations.",
                packageName = "@modelcontextprotocol/server-postgres",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.DATABASE,
                commands = listOf(
                    PluginCommand("query", "Execute SQL query", listOf(
                        CommandParameter("sql", "string", "SQL query to execute")
                    )),
                    PluginCommand("list_tables", "List all tables", emptyList()),
                    PluginCommand("describe_table", "Get table schema", listOf(
                        CommandParameter("table", "string", "Table name")
                    ))
                )
            ),
            MCPPlugin(
                id = "6",
                name = "Slack",
                description = "Integrate with Slack workspaces. Send messages, read channels, manage conversations.",
                packageName = "@modelcontextprotocol/server-slack",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.AUTOMATION,
                commands = listOf(
                    PluginCommand("send_message", "Send a message to a channel", listOf(
                        CommandParameter("channel", "string", "Channel ID"),
                        CommandParameter("text", "string", "Message text")
                    )),
                    PluginCommand("list_channels", "List all channels", emptyList())
                )
            ),
            MCPPlugin(
                id = "7",
                name = "Google Drive",
                description = "Access and manage Google Drive files. Read, write, search, and organize files in Google Drive.",
                packageName = "@modelcontextprotocol/server-gdrive",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.FILE_SYSTEM,
                commands = listOf(
                    PluginCommand("list_files", "List Drive files", listOf(
                        CommandParameter("query", "string", "Search query", false)
                    )),
                    PluginCommand("read_file", "Read file contents", listOf(
                        CommandParameter("file_id", "string", "File ID")
                    )),
                    PluginCommand("create_file", "Create a new file", listOf(
                        CommandParameter("name", "string", "File name"),
                        CommandParameter("content", "string", "File content")
                    ))
                )
            ),
            MCPPlugin(
                id = "8",
                name = "Memory",
                description = "Persistent memory for Claude Code. Store and retrieve information across sessions.",
                packageName = "@modelcontextprotocol/server-memory",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.AI_TOOLS,
                commands = listOf(
                    PluginCommand("store_memory", "Store a memory", listOf(
                        CommandParameter("key", "string", "Memory key"),
                        CommandParameter("value", "string", "Memory value")
                    )),
                    PluginCommand("retrieve_memory", "Retrieve a memory", listOf(
                        CommandParameter("key", "string", "Memory key")
                    )),
                    PluginCommand("list_memories", "List all memories", emptyList())
                )
            ),
            MCPPlugin(
                id = "9",
                name = "Arduino CLI",
                description = "Arduino development tools. Compile, upload, and manage Arduino projects via CLI.",
                packageName = "arduino-cli-mcp",
                repository = "https://github.com/Shubhamai/arduino-cli-mcp",
                category = PluginCategory.DEVELOPMENT,
                commands = listOf(
                    PluginCommand("compile", "Compile Arduino sketch", listOf(
                        CommandParameter("sketch_path", "string", "Path to .ino file"),
                        CommandParameter("fqbn", "string", "Board identifier")
                    )),
                    PluginCommand("upload", "Upload to board", listOf(
                        CommandParameter("port", "string", "Serial port"),
                        CommandParameter("fqbn", "string", "Board identifier")
                    )),
                    PluginCommand("list", "List boards", emptyList())
                )
            ),
            MCPPlugin(
                id = "10",
                name = "Compounding Engineering",
                description = "AI-powered workflows for planning, building, and reviewing code. Makes each unit of work easier through systematic processes.",
                packageName = "@EveryInc/every-marketplace/compounding-engineering",
                repository = "https://github.com/EveryInc/every-marketplace",
                category = PluginCategory.DEVELOPMENT,
                commands = listOf(
                    PluginCommand("plan", "Convert ideas into detailed GitHub issues", emptyList()),
                    PluginCommand("work", "Execute plans using isolated worktrees", emptyList()),
                    PluginCommand("review", "Multi-agent code review for security/performance", emptyList()),
                    PluginCommand("triage", "Present findings for review", emptyList()),
                    PluginCommand("resolve_todo_parallel", "Resolve multiple todos in parallel", emptyList()),
                    PluginCommand("generate_command", "Create Claude Code commands", emptyList())
                )
            ),
            MCPPlugin(
                id = "11",
                name = "Claude Code PM (CCPM)",
                description = "Comprehensive project management with PRDs, epics, and GitHub integration. Spec-driven development with traceability.",
                packageName = "ccpm",
                repository = "https://github.com/automazeio/ccpm",
                category = PluginCategory.AUTOMATION,
                commands = listOf(
                    PluginCommand("prd-new", "Create new PRD through brainstorming", emptyList()),
                    PluginCommand("prd-list", "List all PRDs", emptyList()),
                    PluginCommand("epic-decompose", "Break PRD into technical epics", emptyList()),
                    PluginCommand("epic-sync", "Push epics to GitHub", emptyList()),
                    PluginCommand("issue-start", "Start working on issue", emptyList()),
                    PluginCommand("issue-close", "Complete and close issue", emptyList()),
                    PluginCommand("next", "Get next task recommendation", emptyList()),
                    PluginCommand("status", "Show project status", emptyList())
                )
            ),
            MCPPlugin(
                id = "12",
                name = "Figma",
                description = "Access Figma designs and components. Read design specs, extract assets, and sync design systems.",
                packageName = "@modelcontextprotocol/server-figma",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.DEVELOPMENT,
                commands = listOf(
                    PluginCommand("get_file", "Get Figma file data", listOf(
                        CommandParameter("file_key", "string", "Figma file key")
                    )),
                    PluginCommand("get_components", "List components in file", listOf(
                        CommandParameter("file_key", "string", "Figma file key")
                    ))
                )
            ),
            MCPPlugin(
                id = "13",
                name = "Jira",
                description = "Manage Jira tickets, epics, and sprints. Create issues, update status, and track project progress.",
                packageName = "@modelcontextprotocol/server-jira",
                repository = "https://github.com/modelcontextprotocol/servers",
                category = PluginCategory.AUTOMATION,
                commands = listOf(
                    PluginCommand("create_issue", "Create new Jira issue", listOf(
                        CommandParameter("project", "string", "Project key"),
                        CommandParameter("summary", "string", "Issue summary"),
                        CommandParameter("type", "string", "Issue type")
                    )),
                    PluginCommand("update_issue", "Update existing issue", listOf(
                        CommandParameter("issue_key", "string", "Issue key"),
                        CommandParameter("fields", "object", "Fields to update")
                    )),
                    PluginCommand("search_issues", "Search Jira issues", listOf(
                        CommandParameter("jql", "string", "JQL query")
                    ))
                )
            ),
            MCPPlugin(
                id = "14",
                name = "Agents Framework",
                description = "Multi-agent orchestration for Claude Code. Coordinate specialized agents for complex workflows.",
                packageName = "agents",
                repository = "https://github.com/wshobson/agents",
                category = PluginCategory.AI_TOOLS,
                commands = listOf(
                    PluginCommand("create_agent", "Create new specialized agent", listOf(
                        CommandParameter("name", "string", "Agent name"),
                        CommandParameter("role", "string", "Agent role/specialty")
                    )),
                    PluginCommand("orchestrate", "Coordinate multiple agents", listOf(
                        CommandParameter("task", "string", "Task to orchestrate")
                    ))
                )
            ),
            MCPPlugin(
                id = "15",
                name = "SpecWeaver",
                description = "Spec-driven development with automated code review. Professional workflow for specification-first coding.",
                packageName = "specweaver",
                repository = "https://github.com/RoniLeor/specWeaver",
                category = PluginCategory.DEVELOPMENT,
                commands = listOf(
                    PluginCommand("create_spec", "Create technical specification", listOf(
                        CommandParameter("feature", "string", "Feature description")
                    )),
                    PluginCommand("implement", "Implement from spec", listOf(
                        CommandParameter("spec_id", "string", "Specification ID")
                    )),
                    PluginCommand("review", "Review against spec", listOf(
                        CommandParameter("spec_id", "string", "Specification ID")
                    ))
                )
            )
        )
    }
}
