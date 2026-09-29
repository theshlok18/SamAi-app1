package dev.shlok.samai.ui.screens.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shlok.samai.ui.components.*
import dev.shlok.samai.ui.theme.*

@Composable
fun ChatScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTasks: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF060D1A), SamBackground))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SamTextSecondary)
                }
                Spacer(Modifier.width(8.dp))
                Text("SAM AI Chat", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SamCyan)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { viewModel.clearChat() }) {
                    Icon(Icons.Filled.DeleteOutline, "Clear", tint = SamTextSecondary)
                }
            }

            GlowingDivider()

            // Messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.messages) { msg ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 }
                    ) {
                        MessageBubble(message = msg)
                    }
                }
            }

            // Thinking indicator
            if (uiState.isThinking) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThinkingDots()
                    Text("SAM is thinking...", fontSize = 12.sp, color = SamTextSecondary)
                }
            }

            GlowingDivider()

            // Input bar
            ChatInputBar(
                text = uiState.inputText,
                isListening = uiState.isListening,
                onTextChange = viewModel::updateInput,
                onSend = viewModel::sendMessage,
                onVoice = viewModel::toggleVoice
            )
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.role == "user"
    val clipboard: ClipboardManager = LocalClipboardManager.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(SamCyan.copy(0.15f))
                    .border(1.dp, SamCyan.copy(0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("S", fontSize = 14.sp, color = SamCyan, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(modifier = Modifier.widthIn(max = 300.dp)) {
            if (message.isThinking) {
                GlassCard {
                    Box(Modifier.padding(16.dp)) { ThinkingDots() }
                }
            } else {
                GlassCard(
                    borderColor = if (isUser) SamBlue.copy(0.4f) else SamCyan.copy(0.3f)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        if (message.content.contains("```")) {
                            CodeMessageContent(message.content)
                        } else {
                            Text(
                                text = message.content,
                                fontSize = 14.sp,
                                color = SamTextPrimary,
                                lineHeight = 22.sp
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(
                                onClick = { clipboard.setText(AnnotatedString(message.content)) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ContentCopy, "Copy",
                                    tint = SamTextDim, modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isUser) Spacer(Modifier.width(8.dp))
    }
}

@Composable
private fun CodeMessageContent(content: String) {
    val parts = content.split("```")
    parts.forEachIndexed { i, part ->
        if (i % 2 == 0) {
            if (part.isNotBlank()) {
                Text(part.trim(), fontSize = 14.sp, color = SamTextPrimary, lineHeight = 22.sp)
            }
        } else {
            val lines = part.lines()
            val lang = lines.firstOrNull()?.trim() ?: ""
            val code = if (lang.isNotBlank() && !lang.contains(" ")) {
                lines.drop(1).joinToString("\n")
            } else part

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0A1520))
                    .border(1.dp, SamCyan.copy(0.2f), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = code.trim(),
                    fontSize = 12.sp,
                    color = Color(0xFF9EDBFF),
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ThinkingDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        repeat(3) { i ->
            val alpha by infiniteTransition.animateFloat(
                initialValue = 0.2f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(600, delayMillis = i * 200, easing = EaseInOut),
                    RepeatMode.Reverse
                ),
                label = "dot$i"
            )
            Box(
                Modifier
                    .size(6.dp)
                    .background(SamCyan.copy(alpha), CircleShape)
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    text: String,
    isListening: Boolean,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoice: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text("Ask SAM anything...", color = SamTextDim, fontSize = 14.sp)
            },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SamCyan.copy(0.6f),
                unfocusedBorderColor = SamCyan.copy(0.2f),
                focusedTextColor = SamTextPrimary,
                unfocusedTextColor = SamTextPrimary,
                cursorColor = SamCyan,
                focusedContainerColor = SamSurface,
                unfocusedContainerColor = SamSurface
            ),
            shape = RoundedCornerShape(24.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )

        // Voice button
        IconButton(
            onClick = onVoice,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (isListening) SamCyan.copy(0.2f) else SamSurface)
                .border(1.dp, if (isListening) SamCyan else SamCyan.copy(0.3f), CircleShape)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = "Voice",
                tint = if (isListening) SamCyan else SamTextSecondary
            )
        }

        // Send button
        IconButton(
            onClick = onSend,
            enabled = text.isNotBlank(),
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (text.isNotBlank()) SamCyan.copy(0.2f) else SamSurface)
                .border(1.dp, if (text.isNotBlank()) SamCyan else SamCyan.copy(0.2f), CircleShape)
        ) {
            Icon(
                Icons.Filled.Send, "Send",
                tint = if (text.isNotBlank()) SamCyan else SamTextDim
            )
        }
    }
}
