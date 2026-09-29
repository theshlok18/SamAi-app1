package dev.shlok.samai.ui.screens.home

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.*
import dev.shlok.samai.ui.components.*
import dev.shlok.samai.ui.components.SamCoreState
import dev.shlok.samai.ui.theme.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onNavigateToChat: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToCoding: () -> Unit,
    onNavigateToMemory: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeTask by viewModel.activeTask.collectAsStateWithLifecycle()

    val micPermission = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)

    val coreState = when {
        uiState.isThinking -> SamCoreState.THINKING
        uiState.isListening -> SamCoreState.LISTENING
        activeTask != null -> SamCoreState.EXECUTING
        else -> SamCoreState.IDLE
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF060D1A), SamBackground, Color(0xFF080D18))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .systemBarsPadding()
        ) {
            // Top bar
            HomeTopBar(
                isOnline = uiState.isOnline,
                onSettingsClick = onNavigateToSettings
            )

            Spacer(Modifier.height(16.dp))

            // Greeting
            Column(Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Hello, ${uiState.userName}",
                    fontSize = 14.sp,
                    color = SamTextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "How can I help you today?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamTextPrimary
                )
            }

            Spacer(Modifier.height(24.dp))

            // SAM Core
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                SamCore(
                    state = coreState,
                    size = 200.dp,
                    voiceLevel = uiState.voiceLevel
                )
            }

            Spacer(Modifier.height(16.dp))

            // Response area
            if (uiState.samResponse.isNotBlank()) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Text(
                        text = uiState.samResponse,
                        fontSize = 14.sp,
                        color = SamTextPrimary,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            // Voice Waveform
            VoiceWaveform(
                isActive = uiState.isListening,
                level = uiState.voiceLevel,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Talk button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                VoiceButton(
                    isListening = uiState.isListening,
                    onClick = {
                        if (micPermission.status.isGranted) {
                            viewModel.toggleListening()
                        } else {
                            micPermission.launchPermissionRequest()
                        }
                    }
                )
            }

            Spacer(Modifier.height(28.dp))

            // Quick action cards
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                color = SamTextDim,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(Modifier.height(12.dp))

            QuickActionsGrid(
                onPhoneControl = onNavigateToTasks,
                onTasks = onNavigateToTasks,
                onCoding = onNavigateToCoding,
                onKnowledge = onNavigateToChat,
                onMemory = onNavigateToMemory,
                onChat = onNavigateToChat
            )

            Spacer(Modifier.height(80.dp))
        }

        // Bottom Navigation
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            SamBottomNav(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "chat" -> onNavigateToChat()
                        "tasks" -> onNavigateToTasks()
                        "coding" -> onNavigateToCoding()
                        "settings" -> onNavigateToSettings()
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeTopBar(isOnline: Boolean, onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "SAM AI",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = SamCyan,
                letterSpacing = 4.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .size(6.dp)
                        .background(
                            if (isOnline) SamSuccess else SamError,
                            CircleShape
                        )
                )
                Text(
                    text = if (isOnline) "Online" else "Offline",
                    fontSize = 11.sp,
                    color = if (isOnline) SamSuccess else SamError
                )
            }
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = SamTextSecondary
            )
        }
    }
}

@Composable
private fun VoiceButton(isListening: Boolean, onClick: () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = if (isListening) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "voiceButtonScale"
    )
    val infiniteTransition = rememberInfiniteTransition(label = "voiceBtn")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.0f,
        animationSpec = infiniteRepeatable(tween(1000, easing = EaseOut), RepeatMode.Restart),
        label = "pulse"
    )

    Box(contentAlignment = Alignment.Center) {
        // Pulse ring when listening
        if (isListening) {
            Box(
                Modifier
                    .size(84.dp)
                    .scale(scale + pulseAlpha)
                    .background(SamCyan.copy(alpha = pulseAlpha * 0.3f), CircleShape)
            )
        }
        Box(
            modifier = Modifier
                .scale(scale)
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (isListening) {
                        Brush.radialGradient(listOf(SamCyan.copy(0.5f), SamCyan.copy(0.2f)))
                    } else {
                        Brush.radialGradient(listOf(SamSurfaceVariant, SamSurface))
                    }
                )
                .border(
                    2.dp,
                    if (isListening) SamCyan else SamCyan.copy(0.4f),
                    CircleShape
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                ) { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                contentDescription = if (isListening) "Stop" else "Talk to SAM",
                tint = if (isListening) SamCyan else SamTextSecondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
private fun QuickActionsGrid(
    onPhoneControl: () -> Unit,
    onTasks: () -> Unit,
    onCoding: () -> Unit,
    onKnowledge: () -> Unit,
    onMemory: () -> Unit,
    onChat: () -> Unit
) {
    val cards = listOf(
        Triple("Phone Control", Icons.Filled.PhoneAndroid, onPhoneControl),
        Triple("Tasks", Icons.Filled.PlayArrow, onTasks),
        Triple("Coding", Icons.Filled.Code, onCoding),
        Triple("Knowledge", Icons.Filled.AutoStories, onKnowledge),
        Triple("Memory", Icons.Filled.Psychology, onMemory),
        Triple("Chat", Icons.Filled.Chat, onChat)
    )

    Column(Modifier.padding(horizontal = 16.dp)) {
        for (rowIdx in 0 until 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (colIdx in 0 until 3) {
                    val idx = rowIdx * 3 + colIdx
                    val (label, icon, action) = cards[idx]
                    QuickActionCard(
                        label = label,
                        icon = icon,
                        onClick = action,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            if (rowIdx < 1) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier.aspectRatio(1f),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = SamCyan,
                modifier = Modifier.size(26.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                color = SamTextSecondary,
                letterSpacing = 0.5.sp
            )
        }
    }
}
