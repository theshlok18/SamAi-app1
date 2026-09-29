package dev.shlok.samai.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shlok.samai.profile.DeveloperProfile
import dev.shlok.samai.ui.components.GlassCard
import dev.shlok.samai.ui.components.GlowingDivider
import dev.shlok.samai.ui.theme.*

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAccessibility: () -> Unit,
    onNavigateToAIModels: () -> Unit,
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
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SamTextSecondary)
                }
                Text("Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SamCyan)
            }
            GlowingDivider()

            Spacer(Modifier.height(16.dp))

            // PERSONALIZATION
            SettingsSectionHeader("PERSONALIZATION")
            PersonalizationSection(
                userName = uiState.userProfile.userName,
                assistantName = uiState.userProfile.assistantName,
                wakeWord = uiState.userProfile.wakeWord,
                responseStyle = uiState.userProfile.responseStyle,
                onUserNameChange = viewModel::updateUserName,
                onAssistantNameChange = viewModel::updateAssistantName,
                onWakeWordChange = viewModel::updateWakeWord,
                onResponseStyleChange = viewModel::updateResponseStyle
            )

            Spacer(Modifier.height(16.dp))

            // AI MODELS
            SettingsSectionHeader("AI")
            SettingsNavItem(
                icon = Icons.Filled.SmartToy,
                title = "AI Models",
                subtitle = "Configure OpenAI, Gemini and more",
                onClick = onNavigateToAIModels
            )

            Spacer(Modifier.height(16.dp))

            // ANDROID CONTROL
            SettingsSectionHeader("ANDROID CONTROL")
            SettingsNavItem(
                icon = Icons.Filled.Accessibility,
                title = "Accessibility Service",
                subtitle = if (uiState.accessibilityConnected) "Connected" else "Not connected",
                subtitleColor = if (uiState.accessibilityConnected) SamSuccess else SamWarning,
                onClick = onNavigateToAccessibility
            )

            Spacer(Modifier.height(16.dp))

            // DEVELOPER
            SettingsSectionHeader("DEVELOPER")
            DeveloperSection()

            Spacer(Modifier.height(16.dp))

            // ABOUT
            SettingsSectionHeader("ABOUT")
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    AboutRow("App", "SAM AI")
                    AboutRow("Full Name", "Smart Autonomous Machine")
                    AboutRow("Version", "1.0.0")
                    AboutRow("Developer", DeveloperProfile.NAME)
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        fontSize = 11.sp,
        color = SamTextDim,
        letterSpacing = 2.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsNavItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    subtitleColor: Color = SamTextSecondary,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = SamCyan, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = SamTextPrimary, fontWeight = FontWeight.Medium)
                Text(subtitle, fontSize = 12.sp, color = subtitleColor)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = SamTextDim)
        }
    }
}

@Composable
private fun PersonalizationSection(
    userName: String,
    assistantName: String,
    wakeWord: String,
    responseStyle: String,
    onUserNameChange: (String) -> Unit,
    onAssistantNameChange: (String) -> Unit,
    onWakeWordChange: (String) -> Unit,
    onResponseStyleChange: (String) -> Unit
) {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            EditableSettingRow("Your Name", userName, onUserNameChange)
            EditableSettingRow("Assistant Name", assistantName, onAssistantNameChange)
            EditableSettingRow("Wake Word", wakeWord, onWakeWordChange)
            ResponseStyleSelector(responseStyle, onResponseStyleChange)
        }
    }
}

@Composable
private fun EditableSettingRow(label: String, value: String, onSave: (String) -> Unit) {
    var editing by remember { mutableStateOf(false) }
    var text by remember(value) { mutableStateOf(value) }

    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = SamTextDim)
            if (editing) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SamCyan.copy(0.6f),
                        unfocusedBorderColor = SamCyan.copy(0.2f),
                        focusedTextColor = SamTextPrimary,
                        unfocusedTextColor = SamTextPrimary,
                        cursorColor = SamCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(value, fontSize = 14.sp, color = SamTextPrimary)
            }
        }
        if (editing) {
            IconButton(onClick = { onSave(text); editing = false }) {
                Icon(Icons.Filled.Check, null, tint = SamSuccess)
            }
        } else {
            IconButton(onClick = { editing = true }) {
                Icon(Icons.Filled.Edit, null, tint = SamTextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ResponseStyleSelector(current: String, onChange: (String) -> Unit) {
    val styles = listOf("concise", "balanced", "detailed")
    Column {
        Text("Response Style", fontSize = 11.sp, color = SamTextDim)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            styles.forEach { style ->
                FilterChip(
                    selected = current == style,
                    onClick = { onChange(style) },
                    label = {
                        Text(
                            style.replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SamCyan.copy(0.2f),
                        selectedLabelColor = SamCyan,
                        containerColor = SamSurface,
                        labelColor = SamTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = SamCyan.copy(0.2f),
                        selectedBorderColor = SamCyan.copy(0.5f),
                        enabled = true,
                        selected = current == style
                    )
                )
            }
        }
    }
}

@Composable
private fun DeveloperSection() {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SamCyan.copy(0.15f))
                        .border(1.dp, SamCyan.copy(0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        DeveloperProfile.NAME.first().toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamCyan
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(DeveloperProfile.NAME, fontSize = 16.sp, color = SamTextPrimary, fontWeight = FontWeight.Bold)
                    Text(DeveloperProfile.ROLE, fontSize = 12.sp, color = SamTextSecondary)
                    Text(DeveloperProfile.DESCRIPTION, fontSize = 11.sp, color = SamTextDim)
                }
            }
            Spacer(Modifier.height(12.dp))
            AboutRow("GitHub", DeveloperProfile.GITHUB)
            AboutRow("Instagram", DeveloperProfile.INSTAGRAM)
            Spacer(Modifier.height(4.dp))
            Text(DeveloperProfile.INSPIRATION, fontSize = 11.sp, color = SamTextDim, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = SamTextSecondary)
        Text(value, fontSize = 12.sp, color = SamTextPrimary, fontWeight = FontWeight.Medium)
    }
}
