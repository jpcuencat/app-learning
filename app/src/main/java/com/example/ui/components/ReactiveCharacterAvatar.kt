package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.CharacterMood
import com.example.ui.theme.LocalSemanticColors

@Composable
fun ReactiveCharacterBadge(
    roleEs: String,
    mood: CharacterMood,
    reactionEs: String,
    modifier: Modifier = Modifier
) {
    val semantic = LocalSemanticColors.current
    val bgColor by animateColorAsState(
        targetValue = when (mood) {
            CharacterMood.DELIGHTED -> semantic.successContainer
            CharacterMood.CONFUSED -> MaterialTheme.colorScheme.errorContainer
            CharacterMood.ENCOURAGING -> semantic.warningContainer
            CharacterMood.LISTENING -> MaterialTheme.colorScheme.primaryContainer
            CharacterMood.THINKING -> MaterialTheme.colorScheme.secondaryContainer
            CharacterMood.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "mood_bg"
    )
    val accentColor by animateColorAsState(
        targetValue = when (mood) {
            CharacterMood.DELIGHTED -> semantic.success
            CharacterMood.CONFUSED -> MaterialTheme.colorScheme.error
            CharacterMood.ENCOURAGING -> semantic.warning
            CharacterMood.LISTENING -> MaterialTheme.colorScheme.primary
            CharacterMood.THINKING -> MaterialTheme.colorScheme.secondary
            CharacterMood.NEUTRAL -> MaterialTheme.colorScheme.primary
        },
        label = "mood_accent"
    )
    val bounceScale by animateFloatAsState(
        targetValue = if (mood == CharacterMood.DELIGHTED || mood == CharacterMood.LISTENING) 1.08f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "mood_bounce"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(34.dp)
                    .scale(bounceScale)
            ) {
                val r = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                // Face circle
                drawCircle(color = accentColor.copy(alpha = 0.2f), radius = r, center = center)
                drawCircle(
                    color = accentColor,
                    radius = r - 1.5f,
                    center = center,
                    style = Stroke(width = 3f)
                )

                val eyeY = center.y - r * 0.2f
                val eyeOffsetX = r * 0.34f
                val eyeR = r * 0.11f

                // Eyes
                if (mood == CharacterMood.DELIGHTED) {
                    // Happy arched eyes
                    drawArc(
                        color = accentColor,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(center.x - eyeOffsetX - eyeR * 1.4f, eyeY - eyeR),
                        size = Size(eyeR * 2.8f, eyeR * 2.4f),
                        style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = accentColor,
                        startAngle = 190f,
                        sweepAngle = 160f,
                        useCenter = false,
                        topLeft = Offset(center.x + eyeOffsetX - eyeR * 1.4f, eyeY - eyeR),
                        size = Size(eyeR * 2.8f, eyeR * 2.4f),
                        style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                    )
                } else {
                    drawCircle(color = accentColor, radius = eyeR, center = Offset(center.x - eyeOffsetX, eyeY))
                    drawCircle(color = accentColor, radius = eyeR, center = Offset(center.x + eyeOffsetX, eyeY))
                }

                // Mouth according to mood
                val mouthY = center.y + r * 0.15f
                when (mood) {
                    CharacterMood.DELIGHTED -> {
                        drawArc(
                            color = accentColor,
                            startAngle = 15f,
                            sweepAngle = 150f,
                            useCenter = false,
                            topLeft = Offset(center.x - r * 0.42f, mouthY - r * 0.22f),
                            size = Size(r * 0.84f, r * 0.56f),
                            style = Stroke(width = 3.4f, cap = StrokeCap.Round)
                        )
                    }
                    CharacterMood.CONFUSED -> {
                        drawArc(
                            color = accentColor,
                            startAngle = 200f,
                            sweepAngle = 140f,
                            useCenter = false,
                            topLeft = Offset(center.x - r * 0.36f, mouthY + r * 0.05f),
                            size = Size(r * 0.72f, r * 0.45f),
                            style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                        )
                    }
                    CharacterMood.LISTENING -> {
                        drawCircle(
                            color = accentColor,
                            radius = r * 0.16f,
                            center = Offset(center.x, mouthY + r * 0.14f),
                            style = Stroke(width = 3f)
                        )
                    }
                    CharacterMood.THINKING -> {
                        drawLine(
                            color = accentColor,
                            start = Offset(center.x - r * 0.28f, mouthY + r * 0.16f),
                            end = Offset(center.x + r * 0.28f, mouthY + r * 0.08f),
                            strokeWidth = 3.2f,
                            cap = StrokeCap.Round
                        )
                    }
                    else -> {
                        drawArc(
                            color = accentColor,
                            startAngle = 25f,
                            sweepAngle = 130f,
                            useCenter = false,
                            topLeft = Offset(center.x - r * 0.34f, mouthY - r * 0.12f),
                            size = Size(r * 0.68f, r * 0.42f),
                            style = Stroke(width = 3f, cap = StrokeCap.Round)
                        )
                    }
                }
            }

            androidx.compose.foundation.layout.Column {
                Text(
                    text = roleEs,
                    style = MaterialTheme.typography.labelMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reactionEs,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
