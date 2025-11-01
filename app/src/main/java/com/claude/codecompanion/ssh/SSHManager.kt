package com.claude.codecompanion.ssh

import com.claude.codecompanion.data.ConnectionConfig
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class SSHManager {
    private var session: Session? = null
    private var currentConfig: ConnectionConfig? = null
    private val jsch = JSch()

    suspend fun connect(config: ConnectionConfig): Result<String> = withContext(Dispatchers.IO) {
        try {
            session?.disconnect()

            session = jsch.getSession(config.username, config.host, config.port).apply {
                setPassword(config.password)
                setConfig("StrictHostKeyChecking", "no")
                connect(10000)
            }

            currentConfig = config
            Result.success("Connected to ${config.host}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeCommand(command: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val currentSession = session ?: return@withContext Result.failure(
                Exception("Not connected")
            )

            val channel = currentSession.openChannel("exec") as ChannelExec
            val outputStream = ByteArrayOutputStream()
            val errorStream = ByteArrayOutputStream()

            // Replace $CLAUDE_DIR with the configured working directory
            val processedCommand = currentConfig?.let {
                command.replace("\$CLAUDE_DIR", it.claudeWorkDir)
            } ?: command

            channel.outputStream = outputStream
            channel.setErrStream(errorStream)
            channel.setCommand(processedCommand)
            channel.connect(5000)

            while (!channel.isClosed) {
                Thread.sleep(100)
            }

            val output = outputStream.toString()
            val error = errorStream.toString()

            channel.disconnect()

            if (error.isNotEmpty()) {
                Result.success("$output\n$error")
            } else {
                Result.success(output)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect() {
        session?.disconnect()
        session = null
    }

    fun isConnected(): Boolean = session?.isConnected == true
}
