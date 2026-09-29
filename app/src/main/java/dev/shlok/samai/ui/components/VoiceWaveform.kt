package dev.shlok.samai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import dev.shlok.samai.ui.theme.SamCyan
import kotlin.math.sin

@Composable
fun VoiceWaveform(
    isActive: Boolean,
    level: Float = 0.5f,
    color: Color = SamCyan,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing)),
        label = "phase"
    )

    val barCount = 32
    val animatedHeights = (0 until barCount).map { i ->
        val barPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                tween((600 + i * 20), easing = EaseInOutSine),
                RepeatMode.Reverse
            ),
            label = "bar$i"
        )
        barPhase
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val w = size.width
        val h = size.height
        val centerY = h / 2f
        val barWidth = w / barCount
        val maxH = h * 0.45f

        for (i in 0 until barCount) {
            val x = i * barWidth + barWidth / 2f
            val waveHeight = if (isActive) {
                val sineVal = sin(phase + i * 0.4f).toFloat()
                val normalized = (sineVal + 1f) / 2f
                val dynamic = animatedHeights[i]
                (normalized * 0.6f + dynamic * 0.4f) * level * maxH + 3f
            } else {
                val sineVal = sin(phase * 0.3f + i * 0.3f).toFloat()
                ((sineVal + 1f) / 2f) * 0.15f * maxH + 2f
            }

            val alpha = if (isActive) 0.8f else 0.3f
            drawLine(
                color = color.copy(alpha = alpha),
                start = Offset(x, centerY - waveHeight),
                end = Offset(x, centerY + waveHeight),
                strokeWidth = barWidth * 0.55f,
                cap = StrokeCap.Round
            )
        }
    }
}
