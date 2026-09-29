package dev.shlok.samai.ui.screens.settings

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shlok.samai.ui.components.GlassCard
import dev.shlok.samai.ui.components.GlowingDivider
import dev.shlok.samai.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AccessibilitySetupScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isConnected = uiState.accessibilityConnected

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF060D1A), SamBackground)))
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SamTextSecondary)
                }
                Text(
                    "Accessibility Service",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamCyan
                )
            }
            GlowingDivider()

            Column(
                Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))

                // Status indicator
                Box(
                    Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isConnected) SamSuccess.copy(0.15f)
                            else SamWarning.copy(0.15f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConnected) Icons.Filled.Check else Icons.Filled.Accessibility,
                        contentDescription = null,
                        tint = if (isConnected) SamSuccess else SamWarning,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    if (isConnected) "SAM Accessibility Service" else "Enable SAM Accessibility Service",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SamTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    if (isConnected) "Connected and ready" else "Not connected",
                    fontSize = 14.sp,
                    color = if (isConnected) SamSuccess else SamWarning,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.height(24.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Why is this needed?", fontSize = 14.sp, color = SamCyan, fontWeight = FontWeight.Bold)
                        FeatureRow(Icons.Filled.TouchApp, "Tap and interact with on-screen elements")
                        FeatureRow(Icons.Filled.Navigation, "Navigate between apps and screens")
                        FeatureRow(Icons.Filled.Swipe, "Scroll and swipe for multi-step tasks")
                        FeatureRow(Icons.Filled.Keyboard, "Type text in input fields")
                        FeatureRow(Icons.Filled.Security, "All actions are only taken with your request")
                    }
                }

                Spacer(Modifier.height(24.dp))

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Privacy", fontSize = 14.sp, color = SamCyan, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "SAM only accesses screen content when actively executing a task you requested. " +
                                    "No recording or background monitoring occurs.",
                            fontSize = 13.sp,
                            color = SamTextSecondary,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (!isConnected) {
                    // We'd normally use hilt injection here - using a composable-accessible approach
                    OpenAccessibilityButton()
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = SamSuccess, modifier = Modifier.size(20.dp))
                        Text("SAM Accessibility Service is active", color = SamSuccess, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun OpenAccessibilityButton() {
    val context = androidx.compose.ui.platform.LocalContext.current

    Button(
        onClick = {
            val intent = android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SamCyan.copy(0.2f)),
        border = BorderStroke(1.dp, SamCyan.copy(0.6f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Filled.OpenInNew, null, tint = SamCyan, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Open Accessibility Settings", color = SamCyan, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = SamCyan.copy(0.7f), modifier = Modifier.size(16.dp))
        Text(text, fontSize = 13.sp, color = SamTextSecondary)
    }
}
