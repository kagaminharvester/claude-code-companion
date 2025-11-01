package com.claude.codecompanion.data

data class Task(
    val id: String,
    val content: String,
    val status: TaskStatus,
    val activeForm: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED
}

data class Agent(
    val id: String,
    val name: String,
    val description: String,
    val type: AgentType,
    val icon: String
)

enum class AgentType {
    EXPLORE,
    PLAN,
    GENERAL_PURPOSE,
    CODE_REVIEWER,
    TEST_RUNNER,
    CUSTOM
}

data class Command(
    val id: String,
    val name: String,
    val command: String,
    val description: String,
    val category: CommandCategory,
    val isFavorite: Boolean = false
)

enum class CommandCategory {
    GIT,
    FILE_OPERATIONS,
    SEARCH,
    BUILD,
    TEST,
    CUSTOM
}

data class Workflow(
    val id: String,
    val name: String,
    val description: String,
    val steps: List<WorkflowStep>
)

data class WorkflowStep(
    val order: Int,
    val action: String,
    val parameters: Map<String, String>
)

data class ConnectionConfig(
    val host: String = "",
    val port: Int = 22,
    val username: String = "",
    val password: String = "",
    val useKeyAuth: Boolean = false,
    val privateKeyPath: String = ""
)

data class ConnectionState(
    val isConnected: Boolean = false,
    val message: String = "",
    val lastConnected: Long? = null
)
