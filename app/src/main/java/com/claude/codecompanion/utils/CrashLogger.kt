package com.claude.codecompanion.utils

import android.content.Context
import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CrashLogger(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        try {
            logCrash(exception, thread)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            defaultHandler?.uncaughtException(thread, exception)
        }
    }

    private fun logCrash(exception: Throwable, thread: Thread) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val logFileName = "claude_companion_crash_$timestamp.log"

        // Try multiple locations to ensure we can write the log
        val logLocations = listOf(
            // External storage (Download folder)
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), logFileName),
            // App's external files directory
            File(context.getExternalFilesDir(null), logFileName),
            // App's internal files directory (fallback)
            File(context.filesDir, logFileName)
        )

        var logWritten = false

        for (logFile in logLocations) {
            try {
                logFile.parentFile?.mkdirs()

                FileWriter(logFile, false).use { fileWriter ->
                    PrintWriter(fileWriter).use { printWriter ->
                        // Write crash header
                        printWriter.println("=".repeat(80))
                        printWriter.println("CLAUDE CODE COMPANION - CRASH REPORT")
                        printWriter.println("=".repeat(80))
                        printWriter.println()

                        // Timestamp
                        printWriter.println("Crash Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}")
                        printWriter.println("Thread: ${thread.name}")
                        printWriter.println()

                        // App info
                        printWriter.println("App Version: 1.0")
                        printWriter.println("Package: ${context.packageName}")
                        printWriter.println()

                        // Device info
                        printWriter.println("Device: ${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}")
                        printWriter.println("Android Version: ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})")
                        printWriter.println()

                        // Exception details
                        printWriter.println("=".repeat(80))
                        printWriter.println("EXCEPTION")
                        printWriter.println("=".repeat(80))
                        printWriter.println()
                        printWriter.println("Exception Type: ${exception.javaClass.name}")
                        printWriter.println("Message: ${exception.message}")
                        printWriter.println()

                        // Stack trace
                        printWriter.println("Stack Trace:")
                        printWriter.println("-".repeat(80))
                        exception.printStackTrace(printWriter)
                        printWriter.println()

                        // Caused by (if any)
                        var cause = exception.cause
                        var causeLevel = 1
                        while (cause != null && causeLevel <= 5) {
                            printWriter.println()
                            printWriter.println("Caused by ($causeLevel): ${cause.javaClass.name}")
                            printWriter.println("Message: ${cause.message}")
                            printWriter.println("-".repeat(80))
                            cause.printStackTrace(printWriter)
                            cause = cause.cause
                            causeLevel++
                        }

                        printWriter.println()
                        printWriter.println("=".repeat(80))
                        printWriter.println("Log file saved to: ${logFile.absolutePath}")
                        printWriter.println("=".repeat(80))
                    }
                }

                android.util.Log.e("CrashLogger", "Crash log saved to: ${logFile.absolutePath}")
                logWritten = true
                break // Successfully wrote log, exit loop

            } catch (e: Exception) {
                android.util.Log.e("CrashLogger", "Failed to write log to ${logFile.absolutePath}: ${e.message}")
            }
        }

        if (!logWritten) {
            android.util.Log.e("CrashLogger", "Failed to write crash log to any location")
        }
    }

    companion object {
        fun initialize(context: Context) {
            Thread.setDefaultUncaughtExceptionHandler(CrashLogger(context.applicationContext))
        }
    }
}
