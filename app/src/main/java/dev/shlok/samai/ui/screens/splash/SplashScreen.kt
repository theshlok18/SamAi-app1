package dev.shlok.samai.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shlok.samai.ui.components.SamCore
import dev.shlok.samai.ui.components.SamCoreState
import dev.shlok.samai.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashReady: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    var coreState by remember { mutableStateOf(SamCoreState.IDLE) }

    LaunchedEffect(Unit) {
        onSplashReady()
        delay(400)
        coreState = SamCoreState.THINKING
        titleAlpha.animateTo(1f, animationSpec = tween(800))
        delay(200)
        subtitleAlpha.animateTo(1f, animationSpec = tween(600))
        delay(600)
        coreState = SamCoreState.IDLE
        delay(400)
        onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF0A1525), SamBackground),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SamCore(
                state = coreState,
                size = 200.dp
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "SAM AI",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = SamCyan,
                letterSpacing = 8.sp,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Smart Autonomous Machine",
                fontSize = 13.sp,
                color = SamTextSecondary,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}
