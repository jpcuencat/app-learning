package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.model.CharacterMood
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Full-screen edge-to-edge living New York City world backdrop with interactive camera pan,
 * subtle cinematic camera drift, and real-time location-tailored Canvas VFX
 * (espresso steam, passing yellow cabs, glowing MTA globes, Manhattan skyline lights).
 */
@Composable
fun NycFullScreenBackdrop(
    @DrawableRes imageRes: Int,
    theme: NycSceneTheme,
    cameraPanOffsetPx: Float,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "nyc_world_vfx")
    val driftProgress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "camera_drift"
    )
    val vfxClock by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vfx_clock"
    )

    val animatedPan by animateFloatAsState(
        targetValue = cameraPanOffsetPx,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "pan_smooth"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Full-bleed NYC Scene Image with interactive look-around parallax
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val scale = 1.18f + driftProgress * 0.05f
                    scaleX = scale
                    scaleY = scale
                    translationX = animatedPan + (driftProgress - 0.5f) * 18f
                    translationY = (driftProgress - 0.5f) * -10f
                }
        )

        // 2. Live Atmospheric NYC Canvas VFX tailored to the exact location
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val tau = (2.0 * PI).toFloat()

            when (theme) {
                NycSceneTheme.CAFE, NycSceneTheme.RESTAURANT -> {
                    // Warm floating café/bistro bokeh lights
                    val bokehPositions = listOf(
                        Triple(0.14f, 0.22f, 26f),
                        Triple(0.84f, 0.18f, 32f),
                        Triple(0.76f, 0.34f, 18f),
                        Triple(0.24f, 0.38f, 20f),
                        Triple(0.90f, 0.44f, 24f)
                    )
                    bokehPositions.forEachIndexed { idx, (fx, fy, radius) ->
                        val bobY = sin(vfxClock * tau + idx * 1.3f) * 12f
                        val alpha = 0.14f + 0.10f * sin(vfxClock * tau + idx * 0.9f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFFD670).copy(alpha = alpha.coerceIn(0.05f, 0.28f)),
                                    Color.Transparent
                                ),
                                center = Offset(w * fx + animatedPan * 0.35f, h * fy + bobY),
                                radius = radius * 2.2f
                            ),
                            radius = radius * 2.2f,
                            center = Offset(w * fx + animatedPan * 0.35f, h * fy + bobY)
                        )
                    }

                    // Gentle rising espresso steam ribbons on left & right counter edges
                    for (i in 0..2) {
                        val baseX = w * (0.16f + i * 0.34f) + animatedPan * 0.25f
                        val rise = (vfxClock + i * 0.33f) % 1f
                        val steamAlpha = (sin(rise * PI).toFloat() * 0.16f).coerceIn(0f, 0.18f)
                        val startY = h * 0.56f - rise * h * 0.22f
                        val path = Path().apply {
                            moveTo(baseX, startY + 60f)
                            cubicTo(
                                baseX - 24f, startY + 30f,
                                baseX + 26f, startY,
                                baseX - 8f, startY - 46f
                            )
                        }
                        drawPath(
                            path = path,
                            color = Color.White.copy(alpha = steamAlpha),
                            style = Stroke(width = 7f, cap = StrokeCap.Round)
                        )
                    }
                }

                NycSceneTheme.STREET -> {
                    // Passing blurred NYC yellow cab light streaks across mid-avenue
                    val cab1Progress = (vfxClock * 1.4f) % 1f
                    val cab1X = -w * 0.2f + cab1Progress * w * 1.4f
                    val cabY = h * 0.44f
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x88FCCC0A),
                                Color(0xCCFFE566),
                                Color.Transparent
                            ),
                            startX = cab1X - 110f,
                            endX = cab1X + 110f
                        ),
                        topLeft = Offset(cab1X - 110f, cabY),
                        size = Size(220f, 10f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )

                    // Opposite-lane red/amber brake light streak
                    val cab2Progress = ((vfxClock * 1.1f) + 0.5f) % 1f
                    val cab2X = w * 1.2f - cab2Progress * w * 1.4f
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x77FF5E5B),
                                Color(0x99FF9F1C),
                                Color.Transparent
                            ),
                            startX = cab2X - 95f,
                            endX = cab2X + 95f
                        ),
                        topLeft = Offset(cab2X - 95f, cabY + 26f),
                        size = Size(190f, 7f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )

                    // Iconic green NYC subway entrance globe glow
                    val globePulse = 0.18f + 0.10f * sin(vfxClock * tau)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF00C875).copy(alpha = globePulse), Color.Transparent),
                            center = Offset(w * 0.85f + animatedPan * 0.3f, h * 0.26f),
                            radius = 90f
                        ),
                        radius = 90f,
                        center = Offset(w * 0.85f + animatedPan * 0.3f, h * 0.26f)
                    )
                }

                NycSceneTheme.HOTEL, NycSceneTheme.OFFICE_AIRPORT -> {
                    // Twinkling Manhattan skyline golden window lights
                    val windows = listOf(
                        Pair(0.12f, 0.20f), Pair(0.18f, 0.28f), Pair(0.28f, 0.16f),
                        Pair(0.74f, 0.20f), Pair(0.84f, 0.26f), Pair(0.90f, 0.18f),
                        Pair(0.80f, 0.34f), Pair(0.15f, 0.36f)
                    )
                    windows.forEachIndexed { idx, (fx, fy) ->
                        val twinkle = 0.15f + 0.22f * ((sin(vfxClock * tau * 2f + idx * 1.1f) + 1f) / 2f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0xFFFFE082).copy(alpha = twinkle), Color.Transparent),
                                center = Offset(w * fx + animatedPan * 0.3f, h * fy),
                                radius = 34f
                            ),
                            radius = 34f,
                            center = Offset(w * fx + animatedPan * 0.3f, h * fy)
                        )
                    }
                }
            }
        }

        // 3. Cinematic top & bottom legibility scrim (keeps the living NYC center bright & open!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.00f to Color(0xD9070D16),
                        0.18f to Color(0x5209121E),
                        0.42f to Color(0x2E09121E),
                        0.66f to Color(0xC209121E),
                        0.85f to Color(0xEA080F1A),
                        1.00f to Color(0xF7070D16)
                    )
                )
        )
    }
}

/**
 * First-person expressive animated NYC character standing directly in front of the player
 * on the street, behind the coffee bar, or at the hotel desk.
 * Features realistic blinking, idle breathing, live lip-sync when TTS speaks, and mood poses.
 */
@Composable
fun NycFirstPersonCharacter(
    theme: NycSceneTheme,
    mood: CharacterMood,
    isSpeaking: Boolean,
    partnerName: String,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "nyc_character_anim")
    val breath by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )
    val blinkClock by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )
    val talkPhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 240, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "talk"
    )

    val headTilt by animateFloatAsState(
        targetValue = when (mood) {
            CharacterMood.CONFUSED -> 8f
            CharacterMood.LISTENING -> -5f
            CharacterMood.DELIGHTED -> -3f
            CharacterMood.ENCOURAGING -> 4f
            else -> 0f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "head_tilt"
    )
    val bounceScale by animateFloatAsState(
        targetValue = when (mood) {
            CharacterMood.DELIGHTED -> 1.07f
            CharacterMood.LISTENING -> 1.04f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "char_scale"
    )

    // Outfit & look palette driven by scene & character
    val jacketColor = when (theme) {
        NycSceneTheme.CAFE -> Color(0xFF1A5E52) // SoHo barista forest-green apron
        NycSceneTheme.STREET -> Color(0xFFD96B38) // Warm Manhattan autumn puffer
        NycSceneTheme.HOTEL -> Color(0xFF1F2D45) // Midtown concierge tailored blazer
        NycSceneTheme.RESTAURANT -> Color(0xFF3E2723) // Brooklyn bistro vest
        NycSceneTheme.OFFICE_AIRPORT -> Color(0xFF283A4E) // Wall St / JFK slate coat
    }
    val accentPropColor = when (theme) {
        NycSceneTheme.CAFE -> Color(0xFFF4EFE6) // NYC Coffee cup
        NycSceneTheme.STREET -> Color(0xFFFCCC0A) // NYC yellow scarf
        NycSceneTheme.HOTEL -> Color(0xFFE6C068) // Brass lapel badge
        NycSceneTheme.RESTAURANT -> Color(0xFFFAFAFA) // Crisp collar
        NycSceneTheme.OFFICE_AIRPORT -> Color(0xFF66D9FF) // Executive tie/lanyard
    }
    val skinTone = when {
        partnerName.startsWith("Maya", ignoreCase = true) ||
            partnerName.startsWith("Jordan", ignoreCase = true) -> Color(0xFFD99B72)
        partnerName.startsWith("Elena", ignoreCase = true) ||
            partnerName.startsWith("Nora", ignoreCase = true) -> Color(0xFFEBC09E)
        else -> Color(0xFFF1C7A5)
    }
    val hairColor = when {
        partnerName.startsWith("Sam", ignoreCase = true) -> Color(0xFF3C281E)
        partnerName.startsWith("Maya", ignoreCase = true) -> Color(0xFF1C181B)
        partnerName.startsWith("Chris", ignoreCase = true) -> Color(0xFF4A3322)
        else -> Color(0xFF261C18)
    }

    val auraColor = when (mood) {
        CharacterMood.DELIGHTED -> Color(0xFF34D399)
        CharacterMood.LISTENING -> Color(0xFF38BDF8)
        CharacterMood.CONFUSED -> Color(0xFFF87171)
        CharacterMood.ENCOURAGING -> Color(0xFFFBBF24)
        CharacterMood.THINKING -> Color(0xFFA78BFA)
        CharacterMood.NEUTRAL -> Color(0xFF2DD4BF)
    }

    Box(
        modifier = modifier.size(134.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = bounceScale
                    scaleY = bounceScale
                    translationY = -breath * 5f
                }
        ) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            // Soft backlit halo separating character from NYC background
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(auraColor.copy(alpha = 0.32f), Color.Transparent),
                    center = Offset(cx, h * 0.48f),
                    radius = w * 0.56f
                ),
                radius = w * 0.56f,
                center = Offset(cx, h * 0.48f)
            )

            // Shoulders & NYC Outfit
            val shoulderTop = h * 0.64f
            val torsoPath = Path().apply {
                moveTo(w * 0.14f, h * 0.98f)
                quadraticTo(w * 0.18f, shoulderTop, cx, shoulderTop - 4f)
                quadraticTo(w * 0.82f, shoulderTop, w * 0.86f, h * 0.98f)
                close()
            }
            drawPath(path = torsoPath, color = jacketColor)

            // Outfit details / Props
            when (theme) {
                NycSceneTheme.CAFE -> {
                    // White t-shirt peek + barista apron straps
                    drawCircle(
                        color = Color(0xFFF5F3EE),
                        radius = w * 0.11f,
                        center = Offset(cx, shoulderTop + 4f)
                    )
                    drawLine(
                        color = Color(0xFFD5A052),
                        start = Offset(cx - w * 0.14f, shoulderTop + 2f),
                        end = Offset(cx - w * 0.14f, h * 0.96f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0xFFD5A052),
                        start = Offset(cx + w * 0.14f, shoulderTop + 2f),
                        end = Offset(cx + w * 0.14f, h * 0.96f),
                        strokeWidth = 6f,
                        cap = StrokeCap.Round
                    )
                    // Steaming NYC paper coffee cup held at right corner
                    drawRoundRect(
                        color = accentPropColor,
                        topLeft = Offset(w * 0.72f, h * 0.70f),
                        size = Size(w * 0.18f, h * 0.24f),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRect(
                        color = Color(0xFFC28E5A),
                        topLeft = Offset(w * 0.72f, h * 0.78f),
                        size = Size(w * 0.18f, h * 0.08f)
                    )
                    drawRoundRect(
                        color = Color(0xFF1F2937),
                        topLeft = Offset(w * 0.70f, h * 0.67f),
                        size = Size(w * 0.22f, h * 0.045f),
                        cornerRadius = CornerRadius(5f, 5f)
                    )
                }

                NycSceneTheme.STREET -> {
                    // Iconic Manhattan cozy scarf wrapped around neck
                    drawRoundRect(
                        color = accentPropColor,
                        topLeft = Offset(cx - w * 0.20f, shoulderTop - 2f),
                        size = Size(w * 0.40f, h * 0.11f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                }

                NycSceneTheme.HOTEL, NycSceneTheme.OFFICE_AIRPORT -> {
                    // Crisp white V collar + gold concierge lapel pin
                    val vCollar = Path().apply {
                        moveTo(cx - w * 0.11f, shoulderTop)
                        lineTo(cx, shoulderTop + h * 0.16f)
                        lineTo(cx + w * 0.11f, shoulderTop)
                        close()
                    }
                    drawPath(vCollar, color = Color(0xFFF8FAFC))
                    drawRoundRect(
                        color = accentPropColor,
                        topLeft = Offset(cx + w * 0.13f, shoulderTop + h * 0.12f),
                        size = Size(w * 0.13f, h * 0.055f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                }

                NycSceneTheme.RESTAURANT -> {
                    val vCollar = Path().apply {
                        moveTo(cx - w * 0.11f, shoulderTop)
                        lineTo(cx, shoulderTop + h * 0.15f)
                        lineTo(cx + w * 0.11f, shoulderTop)
                        close()
                    }
                    drawPath(vCollar, color = Color(0xFFFFFBF5))
                }
            }

            // Neck
            drawRoundRect(
                color = skinTone.copy(alpha = 0.92f),
                topLeft = Offset(cx - w * 0.075f, h * 0.54f),
                size = Size(w * 0.15f, h * 0.14f),
                cornerRadius = CornerRadius(12f, 12f)
            )

            // Head (rotates gently with character mood!)
            val headCenter = Offset(cx, h * 0.40f)
            val headR = w * 0.225f

            rotate(degrees = headTilt, pivot = headCenter) {
                // Back hair volume
                drawCircle(
                    color = hairColor,
                    radius = headR * 1.12f,
                    center = Offset(headCenter.x, headCenter.y - headR * 0.12f)
                )

                // Ears
                drawCircle(
                    color = skinTone,
                    radius = headR * 0.22f,
                    center = Offset(headCenter.x - headR * 0.98f, headCenter.y + headR * 0.08f)
                )
                drawCircle(
                    color = skinTone,
                    radius = headR * 0.22f,
                    center = Offset(headCenter.x + headR * 0.98f, headCenter.y + headR * 0.08f)
                )

                // Face oval
                drawCircle(
                    color = skinTone,
                    radius = headR,
                    center = headCenter
                )

                // Soft cheek blush
                drawCircle(
                    color = Color(0xFFE57373).copy(alpha = if (mood == CharacterMood.DELIGHTED) 0.34f else 0.18f),
                    radius = headR * 0.22f,
                    center = Offset(headCenter.x - headR * 0.56f, headCenter.y + headR * 0.20f)
                )
                drawCircle(
                    color = Color(0xFFE57373).copy(alpha = if (mood == CharacterMood.DELIGHTED) 0.34f else 0.18f),
                    radius = headR * 0.22f,
                    center = Offset(headCenter.x + headR * 0.56f, headCenter.y + headR * 0.20f)
                )

                // Stylish top bangs/fringe
                val bangsPath = Path().apply {
                    moveTo(headCenter.x - headR * 1.02f, headCenter.y - headR * 0.15f)
                    quadraticTo(
                        headCenter.x - headR * 0.45f,
                        headCenter.y - headR * 1.18f,
                        headCenter.x + headR * 0.25f,
                        headCenter.y - headR * 0.96f
                    )
                    quadraticTo(
                        headCenter.x + headR * 0.95f,
                        headCenter.y - headR * 0.78f,
                        headCenter.x + headR * 1.02f,
                        headCenter.y - headR * 0.10f
                    )
                    quadraticTo(
                        headCenter.x + headR * 0.25f,
                        headCenter.y - headR * 0.45f,
                        headCenter.x - headR * 1.02f,
                        headCenter.y - headR * 0.15f
                    )
                    close()
                }
                drawPath(bangsPath, color = hairColor)

                // Eyes (with realistic periodic blinking!)
                val isBlinking = blinkClock > 0.94f && mood != CharacterMood.DELIGHTED
                val eyeY = headCenter.y - headR * 0.04f
                val eyeOffsetX = headR * 0.38f

                if (mood == CharacterMood.DELIGHTED) {
                    // Joyful curved smiling eyes
                    listOf(-eyeOffsetX, eyeOffsetX).forEach { dx ->
                        drawArc(
                            color = Color(0xFF1F1A17),
                            startAngle = 195f,
                            sweepAngle = 150f,
                            useCenter = false,
                            topLeft = Offset(headCenter.x + dx - headR * 0.18f, eyeY - headR * 0.10f),
                            size = Size(headR * 0.36f, headR * 0.28f),
                            style = Stroke(width = 4.2f, cap = StrokeCap.Round)
                        )
                    }
                } else if (isBlinking) {
                    listOf(-eyeOffsetX, eyeOffsetX).forEach { dx ->
                        drawLine(
                            color = Color(0xFF1F1A17),
                            start = Offset(headCenter.x + dx - headR * 0.15f, eyeY),
                            end = Offset(headCenter.x + dx + headR * 0.15f, eyeY),
                            strokeWidth = 3.8f,
                            cap = StrokeCap.Round
                        )
                    }
                } else {
                    listOf(-eyeOffsetX, eyeOffsetX).forEach { dx ->
                        drawCircle(
                            color = Color(0xFF1F1A17),
                            radius = headR * 0.115f,
                            center = Offset(headCenter.x + dx, eyeY)
                        )
                        // Eye catchlight sparkle
                        drawCircle(
                            color = Color.White,
                            radius = headR * 0.042f,
                            center = Offset(headCenter.x + dx + headR * 0.04f, eyeY - headR * 0.04f)
                        )
                    }
                }

                // Expressive eyebrows
                val browY = eyeY - headR * 0.24f
                val leftBrowTilt = if (mood == CharacterMood.CONFUSED) -6f else 0f
                val rightBrowTilt = if (mood == CharacterMood.CONFUSED) 5f else 0f
                drawLine(
                    color = hairColor,
                    start = Offset(headCenter.x - eyeOffsetX - headR * 0.16f, browY + leftBrowTilt),
                    end = Offset(headCenter.x - eyeOffsetX + headR * 0.16f, browY - leftBrowTilt * 0.5f),
                    strokeWidth = 3.8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = hairColor,
                    start = Offset(headCenter.x + eyeOffsetX - headR * 0.16f, browY - rightBrowTilt * 0.5f),
                    end = Offset(headCenter.x + eyeOffsetX + headR * 0.16f, browY + rightBrowTilt),
                    strokeWidth = 3.8f,
                    cap = StrokeCap.Round
                )

                // Mouth with REAL-TIME LIP-SYNC when TTS speaks!
                val mouthY = headCenter.y + headR * 0.34f
                if (isSpeaking) {
                    val openHeight = headR * (0.12f + talkPhase * 0.20f)
                    drawOval(
                        color = Color(0xFF7B2D26),
                        topLeft = Offset(headCenter.x - headR * 0.20f, mouthY - openHeight * 0.45f),
                        size = Size(headR * 0.40f, openHeight)
                    )
                } else {
                    when (mood) {
                        CharacterMood.DELIGHTED -> {
                            drawArc(
                                color = Color(0xFF7B2D26),
                                startAngle = 10f,
                                sweepAngle = 160f,
                                useCenter = true,
                                topLeft = Offset(headCenter.x - headR * 0.30f, mouthY - headR * 0.14f),
                                size = Size(headR * 0.60f, headR * 0.36f)
                            )
                        }

                        CharacterMood.CONFUSED -> {
                            drawArc(
                                color = Color(0xFF3E2723),
                                startAngle = 205f,
                                sweepAngle = 130f,
                                useCenter = false,
                                topLeft = Offset(headCenter.x - headR * 0.22f, mouthY),
                                size = Size(headR * 0.44f, headR * 0.22f),
                                style = Stroke(width = 3.8f, cap = StrokeCap.Round)
                            )
                        }

                        CharacterMood.LISTENING -> {
                            drawCircle(
                                color = Color(0xFF3E2723),
                                radius = headR * 0.095f,
                                center = Offset(headCenter.x, mouthY + headR * 0.04f),
                                style = Stroke(width = 3.4f)
                            )
                        }

                        else -> {
                            drawArc(
                                color = Color(0xFF3E2723),
                                startAngle = 20f,
                                sweepAngle = 140f,
                                useCenter = false,
                                topLeft = Offset(headCenter.x - headR * 0.26f, mouthY - headR * 0.12f),
                                size = Size(headR * 0.52f, headR * 0.28f),
                                style = Stroke(width = 3.8f, cap = StrokeCap.Round)
                            )
                        }
                    }
                }
            }

            // Floating celebration stars when DELIGHTED
            if (mood == CharacterMood.DELIGHTED) {
                val starAngles = listOf(-2.4f, -1.5f, -0.7f)
                starAngles.forEachIndexed { idx, angle ->
                    val dist = w * (0.40f + breath * 0.06f)
                    val sx = cx + cos(angle) * dist
                    val sy = h * 0.36f + sin(angle) * dist
                    drawCircle(
                        color = if (idx == 1) Color(0xFFFFD166) else Color(0xFF63E6BE),
                        radius = 5.5f + breath * 2f,
                        center = Offset(sx, sy)
                    )
                }
            }
        }
    }
}

/**
 * Brief authentic NYC Subway / Street arrival card when stepping into a lesson scene.
 * Auto-dismisses quickly or immediately on tap.
 */
@Composable
fun NycArrivalTransitionOverlay(
    visible: Boolean,
    locationLabel: String,
    spec: NycWorldPropSpec,
    sceneGoalEs: String,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(220)),
        exit = fadeOut(tween(420))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xE8070C14))
                .clickable { onDismiss() }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0E141D),
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.26f)),
                shadowElevation = 20.dp,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Classic NYC MTA white header rule
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        spec.subwayLines.forEach { (line, color) ->
                            Surface(
                                shape = CircleShape,
                                color = color,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = line,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (color == Color(0xFFFCCC0A)) Color.Black else Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = spec.arrivalStationEs,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF63E6BE),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = locationLabel,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = spec.weatherAndTime,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFFD166),
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "Misión: $sceneGoalEs",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }

                    Text(
                        text = "Toca para entrar ahora · Desliza la escena para mirar alrededor",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.58f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
