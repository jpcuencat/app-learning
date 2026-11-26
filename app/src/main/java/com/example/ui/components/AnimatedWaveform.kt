package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun AnimatedAudioWave(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 7,
    barColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
    amplitudeBoost: Float = 1f,
    height: Dp = 26.dp,
    width: Dp = 56.dp
) {
    val transition = rememberInfiniteTransition(label = "audio_wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
    ) {
        val barWidth = (size.width / (barCount * 1.9f)).coerceAtLeast(3f)
        val gap = (size.width - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)
        val color = if (isActive) barColor else inactiveColor

        val safeBoost = if (amplitudeBoost.isFinite()) amplitudeBoost.coerceIn(0.4f, 1f) else 0.6f

        for (i in 0 until barCount) {
            val normalizedPos = if (isActive) {
                val wave = (sin(phase + i * 0.85f) + 1f) / 2f
                (0.22f + wave * 0.78f * safeBoost).coerceIn(0.18f, 1f)
            } else {
                val staticShape = 0.25f + 0.18f * sin(i * 0.9f + 0.6f)
                staticShape.coerceIn(0.18f, 0.45f)
            }

            val barHeight = size.height * normalizedPos
            val top = (size.height - barHeight) / 2f
            val left = i * (barWidth + gap)

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
