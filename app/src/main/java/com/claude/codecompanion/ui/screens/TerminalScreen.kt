package com.claude.codecompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.claude.codecompanion.data.TerminalLine
import com.claude.codecompanion.data.LineType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    terminalLines: List<TerminalLine>,
    isExecuting: Boolean,
    onExecuteCommand: (String) -> Unit,
    onClearTerminal: () -> Unit,
    modifier: Modifier = Modifier
) {
    var commandInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new lines added
    LaunchedEffect(terminalLines.size) {
        if (terminalLines.isNotEmpty()) {
            listState.animateScrollToItem(terminalLines.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E1E))
    ) {
        // Terminal Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF2D2D30),
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Terminal,
                        contentDescription = null,
                        tint = Color(0xFF00FF00),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Claude Code Terminal",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Row {
                    IconButton(
                        onClick = onClearTerminal,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear Terminal",
                            tint = Color(0xFFAAAAAA),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Terminal Output Area
        SelectionContainer {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                items(terminalLines) { line ->
                    TerminalLineItem(line)
                }

                if (isExecuting) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF00FF00)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Executing...",
                                color = Color(0xFF00FF00),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Command Input Area
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF252526),
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$ ",
                    color = Color(0xFF00FF00),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 16.sp
                )

                TextField(
                    value = commandInput,
                    onValueChange = { commandInput = it },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF00FF00),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp
                    ),
                    placeholder = {
                        Text(
                            text = "Enter command...",
                            color = Color(0xFF666666),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                    },
                    singleLine = true,
                    enabled = !isExecuting
                )

                IconButton(
                    onClick = {
                        if (commandInput.isNotBlank()) {
                            onExecuteCommand(commandInput)
                            commandInput = ""
                        }
                    },
                    enabled = !isExecuting && commandInput.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Execute",
                        tint = if (commandInput.isNotBlank()) Color(0xFF00FF00) else Color(0xFF666666)
                    )
                }
            }
        }
    }
}

@Composable
fun TerminalLineItem(line: TerminalLine) {
    val textColor = when (line.type) {
        LineType.COMMAND -> Color(0xFF569CD6)
        LineType.OUTPUT -> Color(0xFFCCCCCC)
        LineType.ERROR -> Color(0xFFF48771)
        LineType.SUCCESS -> Color(0xFF4EC9B0)
    }

    val prefix = when (line.type) {
        LineType.COMMAND -> "$ "
        LineType.ERROR -> "[ERROR] "
        LineType.SUCCESS -> "[✓] "
        LineType.OUTPUT -> ""
    }

    Text(
        text = prefix + line.text,
        color = textColor,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 2.dp),
        lineHeight = 18.sp
    )
}
