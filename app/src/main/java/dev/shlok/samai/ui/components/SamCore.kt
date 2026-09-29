package dev.shlok.samai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.shlok.samai.ui.theme.*
import kotlin.math.*

enum class SamCoreState { IDLE, LISTENING, THINKING, EXECUTING, SUCCESS, ERROR }

@Composable
fun SamCore(
    state: SamCoreState = SamCoreState.IDLE,
    size: Dp = 180.dp,
    voiceLevel: Float = 0f,
    modifier: Modifier = Modifier
) {
    // Breathing animation for IDLE
    val breatheAnim = rememberInfiniteTransition(label = "breathe")
    val breatheScale by breatheAnim.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            tween(2200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "breatheScale"
    )

    // Ring rotation
    val ring1Rotation by breatheAnim.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = when (state) {
                    SamCoreState.THINKING, SamCoreState.EXECUTING -> 1200
                    SamCoreState.LISTENING -> 2000
                    else -> 5000
                },
                easing = LinearEasing
            ),
            RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ring2Rotation by breatheAnim.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = when (state) {
                    SamCoreState.THINKING, SamCoreState.EXECUTING -> 1800
                    SamCoreState.LISTENING -> 2800
                    else -> 7000
                },
                easing = LinearEasing
            ),
            RepeatMode.Restart
        ),
        label = "ring2"
    )
    val ring3Rotation by breatheAnim.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = when (state) {
                    SamCoreState.THINKING -> 2500
                    else -> 9000
                },
                easing = LinearEasing
            ),
            RepeatMode.Restart
        ),
        label = "ring3"
    )

    // Glow pulse
    val glowPulse by breatheAnim.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            tween(
                durationMillis = when (state) {
                    SamCoreState.LISTENING -> 400
                    SamCoreState.THINKING -> 600
                    SamCoreState.EXECUTING -> 500
                    else -> 2000
                },
                easing = EaseInOutSine
            ),
            RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    // Particle positions
    val particleTime by breatheAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "particles"
    )

    val coreColor = when (state) {
        SamCoreState.IDLE -> SamCyan
        SamCoreState.LISTENING -> Color(0xFF00FFCC)
        SamCoreState.THINKING -> SamPurple
        SamCoreState.EXECUTING -> SamBlue
        SamCoreState.SUCCESS -> SamSuccess
        SamCoreState.ERROR -> SamError
    }

    Canvas(
        modifier = modifier
            .size(size)
    ) {
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val baseRadius = this.size.width / 2f * 0.9f

        drawSamCore(
            center = center,
            baseRadius = baseRadius,
            breatheScale = if (state == SamCoreState.IDLE) breatheScale else 1f,
            ring1Rotation = ring1Rotation,
            ring2Rotation = ring2Rotation,
            ring3Rotation = ring3Rotation,
            glowPulse = glowPulse,
            particleTime = particleTime,
            coreColor = coreColor,
            state = state,
            voiceLevel = voiceLevel
        )
    }
}

private fun DrawScope.drawSamCore(
    center: Offset,
    baseRadius: Float,
    breatheScale: Float,
    ring1Rotation: Float,
    ring2Rotation: Float,
    ring3Rotation: Float,
    glowPulse: Float,
    particleTime: Float,
    coreColor: Color,
    state: SamCoreState,
    voiceLevel: Float
) {
    val r = baseRadius * breatheScale

    // === OUTER GLOW AURA ===
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(coreColor.copy(alpha = 0.08f * glowPulse), Color.Transparent),
            center = center,
            radius = r * 1.4f
        ),
        radius = r * 1.4f,
        center = center
    )

    // === OUTER DECORATIVE RING ===
    rotate(ring3Rotation, center) {
        drawCircle(
            color = coreColor.copy(alpha = 0.15f),
            radius = r * 0.96f,
            center = center,
            style = Stroke(width = 1f)
        )
        // Dashed tick marks on outer ring
        for (i in 0 until 24) {
            val angle = (i * 15f) * (PI / 180f).toFloat()
            val inner = if (i % 6 == 0) r * 0.88f else r * 0.91f
            val outer = r * 0.96f
            drawLine(
                color = coreColor.copy(alpha = if (i % 6 == 0) 0.6f else 0.25f),
                start = Offset(
                    center.x + cos(angle) * inner,
                    center.y + sin(angle) * inner
                ),
                end = Offset(
                    center.x + cos(angle) * outer,
                    center.y + sin(angle) * outer
                ),
                strokeWidth = if (i % 6 == 0) 2f else 1f
            )
        }
    }

    // === RING 1 (rotating) ===
    rotate(ring1Rotation, center) {
        drawCircle(
            color = coreColor.copy(alpha = 0.4f * glowPulse),
            radius = r * 0.78f,
            center = center,
            style = Stroke(width = 2f)
        )
        // Arch gaps to make it look segmented
        for (i in 0 until 4) {
            val startAngle = i * 90f + 10f
            val sweepAngle = 70f
            drawArc(
                color = coreColor.copy(alpha = 0.7f * glowPulse),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - r * 0.78f, center.y - r * 0.78f),
                size = androidx.compose.ui.geometry.Size(r * 1.56f, r * 1.56f),
                style = Stroke(width = 3f)
            )
        }
    }

    // === RING 2 (counter-rotating) ===
    rotate(ring2Rotation, center) {
        for (i in 0 until 3) {
            val startAngle = i * 120f + 20f
            val sweepAngle = 80f
            drawArc(
                color = coreColor.copy(alpha = 0.5f * glowPulse),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - r * 0.62f, center.y - r * 0.62f),
                size = androidx.compose.ui.geometry.Size(r * 1.24f, r * 1.24f),
                style = Stroke(width = 2f)
            )
        }
    }

    // === LISTENING VOICE WAVEFORM RING ===
    if (state == SamCoreState.LISTENING) {
        val waveRadius = r * 0.78f + voiceLevel * r * 0.15f
        drawCircle(
            color = Color(0xFF00FFCC).copy(alpha = 0.3f + voiceLevel * 0.4f),
            radius = waveRadius,
            center = center,
            style = Stroke(width = 3f + voiceLevel * 4f)
        )
    }

    // === INNER FILL ===
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                coreColor.copy(alpha = 0.25f * glowPulse),
                coreColor.copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = center,
            radius = r * 0.5f
        ),
        radius = r * 0.5f,
        center = center
    )

    // === CORE GLOW ===
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.9f),
                coreColor.copy(alpha = 0.9f),
                coreColor.copy(alpha = 0.3f),
                Color.Transparent
            ),
            center = center,
            radius = r * 0.22f
        ),
        radius = r * 0.22f,
        center = center
    )

    // === CORE CENTER ===
    drawCircle(
        color = Color.White,
        radius = r * 0.08f,
        center = center
    )

    // === PARTICLES ===
    val particleCount = when (state) {
        SamCoreState.THINKING, SamCoreState.EXECUTING -> 8
        SamCoreState.LISTENING -> 6
        else -> 4
    }
    for (i in 0 until particleCount) {
        val baseAngle = (i.toFloat() / particleCount) * 2f * PI.toFloat()
        val speed = if (state == SamCoreState.THINKING) 1.5f else 1f
        val angle = baseAngle + particleTime * 2f * PI.toFloat() * speed
        val distance = r * (0.4f + 0.35f * ((i % 3) / 3f))
        val px = center.x + cos(angle) * distance
        val py = center.y + sin(angle) * distance
        drawCircle(
            color = coreColor.copy(alpha = 0.6f * glowPulse),
            radius = 3f,
            center = Offset(px, py)
        )
    }

    // === CROSS HAIR LINES (subtle) ===
    val crossAlpha = 0.12f
    drawLine(
        color = coreColor.copy(alpha = crossAlpha),
        start = Offset(center.x, center.y - r * 0.9f),
        end = Offset(center.x, center.y + r * 0.9f),
        strokeWidth = 1f
    )
    drawLine(
        color = coreColor.copy(alpha = crossAlpha),
        start = Offset(center.x - r * 0.9f, center.y),
        end = Offset(center.x + r * 0.9f, center.y),
        strokeWidth = 1f
    )
}
