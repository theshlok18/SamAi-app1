package dev.shlok.samai.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shlok.samai.ui.components.GlassCard
import dev.shlok.samai.ui.components.GlowingDivider
import dev.shlok.samai.ui.theme.*

@Composable
fun AIModelSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF060D1A), SamBackground)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SamTextSecondary)
                }
                Text("AI Models", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SamCyan)
            }
            GlowingDivider()

            Spacer(Modifier.height(16.dp))

            Text(
                "Select an AI provider and enter your API key. Keys are stored securely on device.",
                fontSize = 13.sp,
                color = SamTextSecondary,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(Modifier.height(20.dp))

            // Default provider selector
            Text("DEFAULT PROVIDER", fontSize = 11.sp, color = SamTextDim, letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            val providers = listOf("openai" to "OpenAI GPT", "gemini" to "Google Gemini", "anthropic" to "Anthropic Claude")
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(8.dp)) {
                    providers.forEach { (id, name) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setProvider(id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.aiSettings.selectedProvider == id,
                                onClick = { viewModel.setProvider(id) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = SamCyan,
                                    unselectedColor = SamTextDim
                                )
                            )
                            Text(name, fontSize = 14.sp, color = SamTextPrimary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // API Keys
            Text("API KEYS", fontSize = 11.sp, color = SamTextDim, letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            ApiKeyCard(
                title = "OpenAI",
                subtitle = "GPT-3.5 / GPT-4",
                currentKey = uiState.aiSettings.openAiKey,
                onSave = viewModel::saveOpenAiKey,
                helpText = "Get your key from platform.openai.com"
            )

            Spacer(Modifier.height(8.dp))

            ApiKeyCard(
                title = "Google Gemini",
                subtitle = "Gemini 1.5 Flash",
                currentKey = uiState.aiSettings.geminiKey,
                onSave = viewModel::saveGeminiKey,
                helpText = "Get your key from aistudio.google.com"
            )

            Spacer(Modifier.height(8.dp))

            ApiKeyCard(
                title = "Anthropic",
                subtitle = "Claude 3",
                currentKey = uiState.aiSettings.anthropicKey,
                onSave = viewModel::saveAnthropicKey,
                helpText = "Get your key from console.anthropic.com"
            )

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ApiKeyCard(
    title: String,
    subtitle: String,
    currentKey: String,
    onSave: (String) -> Unit,
    helpText: String
) {
    var isExpanded by remember { mutableStateOf(false) }
    var keyText by remember(currentKey) { mutableStateOf(currentKey) }
    var showKey by remember { mutableStateOf(false) }

    val isConfigured = currentKey.isNotBlank()

    GlassCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        borderColor = if (isConfigured) SamSuccess.copy(0.4f) else SamCyan.copy(0.2f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(title, fontSize = 15.sp, color = SamTextPrimary, fontWeight = FontWeight.Medium)
                        if (isConfigured) {
                            Box(
                                Modifier
                                    .background(SamSuccess.copy(0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Configured", fontSize = 9.sp, color = SamSuccess)
                            }
                        }
                    }
                    Text(subtitle, fontSize = 12.sp, color = SamTextSecondary)
                }
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    null, tint = SamTextSecondary
                )
            }

            if (isExpanded) {
                Spacer(Modifier.height(12.dp))
                Text(helpText, fontSize = 11.sp, color = SamTextDim)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    placeholder = { Text("Enter API key...", color = SamTextDim, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                if (showKey) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                null, tint = SamTextSecondary
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SamCyan.copy(0.6f),
                        unfocusedBorderColor = SamCyan.copy(0.2f),
                        focusedTextColor = SamTextPrimary,
                        unfocusedTextColor = SamTextPrimary,
                        cursorColor = SamCyan,
                        focusedContainerColor = SamSurface,
                        unfocusedContainerColor = SamSurface
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { onSave(keyText); isExpanded = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = SamCyan.copy(0.2f)),
                    border = BorderStroke(1.dp, SamCyan.copy(0.5f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Key", color = SamCyan)
                }
            }
        }
    }
}
