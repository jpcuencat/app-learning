package com.example.ui.screens

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SlowMotionVideo
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.model.EvaluationOutcome
import com.example.model.EvaluationSource
import com.example.model.InputMode
import com.example.model.TurnFeedback
import com.example.speech.DictationState
import com.example.speech.NycAmbienceKind
import com.example.speech.NycAmbiencePlayer
import com.example.speech.SpeechPlaybackState
import com.example.ui.components.AnimatedAudioWave
import com.example.ui.components.NycArrivalTransitionOverlay
import com.example.ui.components.NycFirstPersonCharacter
import com.example.ui.components.NycFullScreenBackdrop
import com.example.ui.components.NycSceneTheme
import com.example.ui.components.NycWorldPropOverlay
import com.example.ui.components.resolveNycWorldSpec
import com.example.ui.viewmodel.CompletedExchange
import com.example.ui.viewmodel.PracticeSessionState
import kotlinx.coroutines.delay

private val NycGlassDark = Color(0xCC0A1320)
private val NycGlassSurface = Color(0xB80F1B2B)
private val NycEmerald = Color(0xFF34D399)
private val NycAmber = Color(0xFFFFD166)
private val NycTeal = Color(0xFF4BE2CE)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PracticeTurnScreen(
    session: PracticeSessionState,
    speechState: SpeechPlaybackState,
    dictationState: DictationState,
    isOnline: Boolean,
    onBack: () -> Unit,
    onPlayPartnerPhrase: (slow: Boolean) -> Unit,
    onPlayReaction: () -> Unit,
    onPlayModelExample: (String) -> Unit,
    onSwitchInputMode: (InputMode) -> Unit,
    onUpdateAnswerText: (String) -> Unit,
    onAppendSpark: (String) -> Unit,
    onToggleEditTranscript: (Boolean) -> Unit,
    onToggleHint: () -> Unit,
    onTogglePartnerTranslation: () -> Unit,
    onStartDictation: () -> Unit,
    onStopDictation: () -> Unit,
    isInlineSpeechSupported: Boolean,
    buildSpeechIntent: () -> Intent,
    onExternalSpeechResults: (List<String>) -> Unit,
    onExternalSpeechError: (String) -> Unit,
    onSelectAlternative: (String) -> Unit,
    onRetryDictation: () -> Unit,
    onSubmitAnswer: () -> Unit,
    onConsultAiTutor: () -> Unit,
    onEditAfterFeedback: () -> Unit,
    onAdvanceOrFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var micPermissionDenied by remember { mutableStateOf(false) }
    var showPropOverlay by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var showArrivalSplash by remember(session.lesson.id, session.mode) { mutableStateOf(true) }
    var cameraPanPx by remember { mutableFloatStateOf(0f) }
    var ambienceEnabled by remember { mutableStateOf(true) }

    val nycSpec = remember(session.lesson.id, session.mode) {
        resolveNycWorldSpec(
            lessonId = session.lesson.id,
            mode = session.mode,
            locationLabel = session.variant.nycLocationLabel,
            timeLabel = session.variant.nycTimeLabel
        )
    }

    val ambienceKind = remember(nycSpec.theme) {
        when (nycSpec.theme) {
            NycSceneTheme.CAFE, NycSceneTheme.RESTAURANT -> NycAmbienceKind.CAFE
            NycSceneTheme.STREET -> NycAmbienceKind.STREET
            NycSceneTheme.HOTEL, NycSceneTheme.OFFICE_AIRPORT -> NycAmbienceKind.LOBBY
        }
    }

    // Procedural offline NYC ambient soundscape (completely pauses AudioTrack during mic usage)
    val ambiencePlayer = remember { NycAmbiencePlayer() }
    DisposableEffect(ambienceKind) {
        ambiencePlayer.setEnabled(ambienceEnabled, ambienceKind)
        onDispose { ambiencePlayer.release() }
    }
    LaunchedEffect(speechState.isSpeaking) {
        ambiencePlayer.setDucked(speechState.isSpeaking)
    }
    LaunchedEffect(dictationState.isListening) {
        ambiencePlayer.setPausedForMicrophone(dictationState.isListening)
    }

    // Auto-dismiss authentic MTA NYC arrival card after 1.6s
    LaunchedEffect(session.lesson.id, session.mode) {
        showArrivalSplash = true
        delay(1_600L)
        showArrivalSplash = false
    }

    // Typewriter reveal for the live NYC character's dialogue on each turn
    val turn = session.currentTurn
    var revealedLine by remember(turn.id) { mutableStateOf("") }
    LaunchedEffect(turn.id, showArrivalSplash) {
        if (showArrivalSplash) return@LaunchedEffect
        revealedLine = ""
        val full = turn.partnerLineEn
        for (i in 1..full.length) {
            revealedLine = full.substring(0, i)
            delay(18L)
        }
    }

    // Auto-scroll smoothly on turn advance or feedback reveal
    LaunchedEffect(session.turnIndex) {
        scrollState.animateScrollTo(0)
    }
    LaunchedEffect(session.feedback) {
        if (session.feedback != null) {
            delay(80L)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Fallback system voice recognition activity launcher when inline SpeechRecognizer is unavailable
    val speechActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        ambiencePlayer.setPausedForMicrophone(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                .orEmpty()
            onExternalSpeechResults(matches)
        }
    }

    val startVoiceCaptureSafely = {
        ambiencePlayer.setPausedForMicrophone(true)
        if (isInlineSpeechSupported) {
            onStartDictation()
        } else {
            val launchResult = runCatching {
                speechActivityLauncher.launch(buildSpeechIntent())
            }
            if (launchResult.isFailure) {
                ambiencePlayer.setPausedForMicrophone(false)
                onExternalSpeechError(
                    "El reconocimiento de voz no está instalado en este teléfono o emulador. Puedes cambiar a Escribir y continuar en este mismo turno."
                )
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            micPermissionDenied = false
            startVoiceCaptureSafely()
        } else {
            ambiencePlayer.setPausedForMicrophone(false)
            micPermissionDenied = true
        }
    }

    val requestOrStartMic: () -> Unit = {
        ambiencePlayer.setPausedForMicrophone(true)
        val hasPerm = runCatching {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        }.getOrDefault(false)

        if (hasPerm) {
            micPermissionDenied = false
            startVoiceCaptureSafely()
        } else {
            val permLaunch = runCatching {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
            if (permLaunch.isFailure) {
                ambiencePlayer.setPausedForMicrophone(false)
                onExternalSpeechError(
                    "No se pudo solicitar el permiso del micrófono. Cambia a Escribir para responder en este turno."
                )
            }
        }
    }

    val isPartnerSpeaking = speechState.isSpeaking &&
        (speechState.activeTag == "partner_normal" ||
            speechState.activeTag == "partner_slow" ||
            speechState.activeTag == "character_reaction")

    val feedback = session.feedback

    // FULL-SCREEN 100% EDGE-TO-EDGE FIRST-PERSON NEW YORK CITY WORLD
    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    cameraPanPx = (cameraPanPx + dragAmount * 0.45f).coerceIn(-90f, 90f)
                }
            }
    ) {
        // LAYER 0: Living Full-Screen NYC World (animated photo parallax + steam/cabs/skyline VFX)
        NycFullScreenBackdrop(
            imageRes = session.lesson.imageRes,
            theme = nycSpec.theme,
            cameraPanOffsetPx = cameraPanPx
        )

        // LAYER 1: Floating Transparent First-Person HUD + Stage + Action Console
        Column(modifier = Modifier.fillMaxSize()) {
            // 1A. Floating Top NYC HUD (MTA Station Badge, Ambience Toggle, Vibe NYC, XP)
            NycFloatingTopHud(
                session = session,
                subwayLines = nycSpec.subwayLines,
                ambienceEnabled = ambienceEnabled,
                onToggleAmbience = {
                    ambienceEnabled = !ambienceEnabled
                    ambiencePlayer.setEnabled(ambienceEnabled, ambienceKind)
                },
                onToggleHistory = { showHistory = !showHistory },
                onBack = onBack
            )

            // 1B. Scrollable Live NYC Stage & Frosted Interactive Console
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 700.dp)
                        .align(Alignment.TopCenter)
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Expandable live dialogue transcript
                    AnimatedVisibility(
                        visible = showHistory && session.dialogueHistory.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        NycLiveHistoryOverlayStrip(history = session.dialogueHistory)
                    }

                    // CENTERPIECE: First-Person Character Standing Live in NYC + In-World Prop Hotspot
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        // Expressive animated New Yorker facing you
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            NycFirstPersonCharacter(
                                theme = nycSpec.theme,
                                mood = session.characterMood,
                                isSpeaking = isPartnerSpeaking,
                                partnerName = turn.partnerRoleEs
                            )

                            // Character name & live reaction pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = NycGlassDark,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = turn.partnerRoleEs,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = NycAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "· ${session.characterReactionEs}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.85f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        // Tappable In-World NYC Prop Hotspot (Chalkboard Menu / MTA Map / Hotel Card)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xD9101E2E),
                            border = BorderStroke(1.2.dp, NycTeal.copy(alpha = 0.65f)),
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { showPropOverlay = true }
                                .testTag("open_nyc_prop_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = nycSpec.icon,
                                    contentDescription = nycSpec.buttonLabelEs,
                                    tint = NycTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = nycSpec.buttonLabelEs,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // VISUAL-NOVEL FLOATING SPEECH BUBBLE (right beneath the speaking New Yorker)
                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 6.dp,
                            topEnd = 22.dp,
                            bottomStart = 22.dp,
                            bottomEnd = 22.dp
                        ),
                        color = NycGlassDark,
                        border = BorderStroke(1.2.dp, Color.White.copy(alpha = 0.24f)),
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "“${revealedLine.ifBlank { turn.partnerLineEn }}”",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                AnimatedAudioWave(
                                    isActive = isPartnerSpeaking,
                                    barColor = NycTeal
                                )
                            }

                            AnimatedVisibility(visible = session.showPartnerTranslation) {
                                Text(
                                    text = turn.partnerLineEsHelper,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }

                            // Compact frosted audio controls inside the speech bubble
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = NycTeal.copy(alpha = 0.22f),
                                    border = BorderStroke(1.dp, NycTeal.copy(alpha = 0.55f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onPlayPartnerPhrase(false) }
                                        .testTag("listen_phrase_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = null,
                                            tint = NycTeal,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Escuchar frase",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.10f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onPlayPartnerPhrase(true) }
                                        .testTag("listen_slow_button")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 9.dp, horizontal = 10.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.SlowMotionVideo,
                                            contentDescription = null,
                                            tint = NycAmber,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Más lento",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (session.showPartnerTranslation) {
                                        NycAmber.copy(alpha = 0.28f)
                                    } else {
                                        Color.White.copy(alpha = 0.10f)
                                    },
                                    border = BorderStroke(
                                        1.dp,
                                        if (session.showPartnerTranslation) NycAmber else Color.White.copy(alpha = 0.24f)
                                    ),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { onTogglePartnerTranslation() }
                                        .testTag("toggle_translation_button")
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Translate,
                                            contentDescription = "Traducir frase",
                                            tint = if (session.showPartnerTranslation) NycAmber else Color.White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // LIVE BACK-AND-FORTH EXCHANGE WHEN YOU RESPOND & PASS
                    if (session.activeReactionEn != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Player's right-aligned spoken/written bubble
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 6.dp,
                                        bottomStart = 20.dp,
                                        bottomEnd = 20.dp
                                    ),
                                    color = Color(0xD9103B3A),
                                    border = BorderStroke(1.2.dp, NycTeal.copy(alpha = 0.7f)),
                                    modifier = Modifier.widthIn(max = 310.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                        Text(
                                            text = "TÚ EN NYC",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NycTeal,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = "“${session.typedAnswer.trim()}”",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Character's instant spoken NYC reply!
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 6.dp,
                                    topEnd = 20.dp,
                                    bottomStart = 20.dp,
                                    bottomEnd = 20.dp
                                ),
                                color = Color(0xDE0E3026),
                                border = BorderStroke(1.5.dp, NycEmerald),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${turn.partnerRoleEs.substringBefore(" ·")} te responde en vivo:",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NycEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "“${session.activeReactionEn}”",
                                            style = MaterialTheme.typography.titleLarge,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    IconButton(
                                        onClick = onPlayReaction,
                                        modifier = Modifier.testTag("replay_reaction_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Repetir respuesta del personaje",
                                            tint = NycEmerald
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FROSTED TRANSLUCENT PLAYER INTERACTION CONSOLE (floats directly over NYC!)
                    if (feedback == null || feedback.outcome != EvaluationOutcome.PASSED) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = NycGlassSurface,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                // Spanish situation cue + Pista chip
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "TU MOMENTO EN LA ESCENA",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NycAmber,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = turn.taskInstructionEs,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (session.showHint) {
                                            NycAmber.copy(alpha = 0.25f)
                                        } else {
                                            Color.White.copy(alpha = 0.12f)
                                        },
                                        border = BorderStroke(1.dp, NycAmber.copy(alpha = 0.5f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .clickable { onToggleHint() }
                                            .testTag("see_hint_button")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Lightbulb,
                                                contentDescription = null,
                                                tint = NycAmber,
                                                modifier = Modifier.size(15.dp)
                                            )
                                            Text(
                                                text = if (session.showHint) "Ocultar" else "Ver pista",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                AnimatedVisibility(
                                    visible = session.showHint,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut()
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = NycAmber.copy(alpha = 0.16f),
                                        border = BorderStroke(1.dp, NycAmber.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Pista: ${turn.hintEs}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }

                                // Translucent Hablar / Escribir Pills
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val speakSelected = session.inputMode == InputMode.SPEAK
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (speakSelected) NycTeal else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(50))
                                            .clickable { onSwitchInputMode(InputMode.SPEAK) }
                                            .testTag("mode_speak_tab")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Mic,
                                                contentDescription = null,
                                                tint = if (speakSelected) Color(0xFF07131A) else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Hablar",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (speakSelected) Color(0xFF07131A) else Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    val writeSelected = session.inputMode == InputMode.WRITE
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = if (writeSelected) NycTeal else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(50))
                                            .clickable { onSwitchInputMode(InputMode.WRITE) }
                                            .testTag("mode_write_tab")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.Center,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Keyboard,
                                                contentDescription = null,
                                                tint = if (writeSelected) Color(0xFF07131A) else Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Escribir",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = if (writeSelected) Color(0xFF07131A) else Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (session.inputMode == InputMode.SPEAK) {
                                    NycGlassVoiceOrbSection(
                                        session = session,
                                        dictationState = dictationState,
                                        micPermissionDenied = micPermissionDenied,
                                        onUseMic = requestOrStartMic,
                                        onStopDictation = onStopDictation,
                                        onEditTranscript = { onToggleEditTranscript(true) },
                                        onUpdateTranscript = onUpdateAnswerText,
                                        onSelectAlternative = onSelectAlternative,
                                        onRetryDictation = {
                                            val hasPerm = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (hasPerm) onRetryDictation() else requestOrStartMic()
                                        },
                                        onSwitchToWrite = { onSwitchInputMode(InputMode.WRITE) },
                                        onOpenVoiceSettings = {
                                            runCatching {
                                                val intent = Intent(Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
                                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                }
                                                context.startActivity(intent)
                                            }
                                        }
                                    )
                                } else {
                                    NycGlassWriteSection(
                                        typedAnswer = session.typedAnswer,
                                        starterSparks = turn.starterSparksEn,
                                        onUpdateAnswerText = onUpdateAnswerText,
                                        onAppendSpark = onAppendSpark,
                                        onSubmitAnswer = onSubmitAnswer
                                    )
                                }

                                // Quick tappable "Chispas NYC" sparks when idle in voice mode
                                if (session.inputMode == InputMode.SPEAK &&
                                    session.typedAnswer.isBlank() &&
                                    !dictationState.isListening &&
                                    turn.starterSparksEn.isNotEmpty()
                                ) {
                                    NycStarterSparksRow(
                                        label = "Chispas NYC · toca para responder al vuelo",
                                        sparks = turn.starterSparksEn,
                                        onTapSpark = onAppendSpark
                                    )
                                }
                            }
                        }
                    }

                    // Consulting AI Coach spinner
                    AnimatedVisibility(visible = session.isConsultingAiTutor) {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = NycGlassDark,
                            border = BorderStroke(1.dp, NycTeal.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp,
                                    color = NycTeal
                                )
                                Text(
                                    text = "Tu coach de NYC está revisando tu frase…",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Frosted NYC Coach Outcome Card
                    if (feedback != null) {
                        NycGlassFeedbackCard(
                            feedback = feedback,
                            isOnline = isOnline,
                            onPlayExample = { onPlayModelExample(feedback.modelExampleEn) },
                            onConsultAiTutor = onConsultAiTutor
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // 1C. Frosted Glass Bottom Action Bar (Always stays above keyboard!)
            NycGlassBottomActionBar(
                session = session,
                isListening = dictationState.isListening,
                onSubmitAnswer = onSubmitAnswer,
                onAdvanceOrFinish = onAdvanceOrFinish,
                onEditAfterFeedback = onEditAfterFeedback,
                onPlayModelExample = onPlayModelExample
            )
        }

        // LAYER 2: Interactive In-World NYC Prop Modal (Chalkboard Menu / MTA Map / Hotel Card)
        if (showPropOverlay) {
            NycWorldPropOverlay(
                spec = nycSpec,
                onDismiss = { showPropOverlay = false },
                onSpeakPhrase = onPlayModelExample,
                onInsertPhrase = { phrase ->
                    onUpdateAnswerText(phrase)
                    onSwitchInputMode(InputMode.WRITE)
                }
            )
        }

        // LAYER 3: Authentic MTA Subway / Street Arrival Transition
        NycArrivalTransitionOverlay(
            visible = showArrivalSplash,
            locationLabel = session.variant.nycLocationLabel,
            spec = nycSpec,
            sceneGoalEs = session.variant.sceneGoalEs,
            onDismiss = { showArrivalSplash = false }
        )
    }
}

@Composable
private fun NycFloatingTopHud(
    session: PracticeSessionState,
    subwayLines: List<Pair<String, Color>>,
    ambienceEnabled: Boolean,
    onToggleAmbience: () -> Unit,
    onToggleHistory: () -> Unit,
    onBack: () -> Unit
) {
    val animatedVibe by animateFloatAsState(
        targetValue = session.nycVibePercent / 100f,
        animationSpec = tween(450),
        label = "vibe_hud"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = NycGlassDark,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f))
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("practice_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver al mapa",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // MTA NYC Location Badge
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NycGlassDark,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        val firstLine = subwayLines.firstOrNull()
                        if (firstLine != null) {
                            Surface(
                                shape = CircleShape,
                                color = firstLine.second,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = firstLine.first,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (firstLine.second == Color(0xFFFCCC0A)) Color.Black else Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${session.variant.nycLocationLabel} · ${session.variant.nycTimeLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // NYC Ambience Audio Toggle
                Surface(
                    shape = CircleShape,
                    color = if (ambienceEnabled) NycTeal.copy(alpha = 0.24f) else NycGlassDark,
                    border = BorderStroke(
                        1.dp,
                        if (ambienceEnabled) NycTeal.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.22f)
                    )
                ) {
                    IconButton(
                        onClick = onToggleAmbience,
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("toggle_nyc_ambience_button")
                    ) {
                        Icon(
                            imageVector = if (ambienceEnabled) Icons.Filled.Headphones else Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = if (ambienceEnabled) {
                                "Silenciar ambiente de Nueva York"
                            } else {
                                "Activar ambiente de Nueva York"
                            },
                            tint = if (ambienceEnabled) NycTeal else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // XP Pill
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NycGlassDark,
                    border = BorderStroke(1.dp, NycAmber.copy(alpha = 0.55f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = NycAmber,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "+${session.sessionXp}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        // Second slim HUD row: Turn story progress dots + Combo + History + Vibe NYC meter
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(session.totalTurns) { idx ->
                    val isCurrent = idx == session.turnIndex
                    val isDone = idx < session.turnIndex
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isCurrent) 24.dp else 10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                when {
                                    isDone -> NycTeal
                                    isCurrent -> NycAmber
                                    else -> Color.White.copy(alpha = 0.34f)
                                }
                            )
                    )
                }
                Text(
                    text = "Escena ${session.turnIndex + 1}/${session.totalTurns}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.SemiBold
                )
                if (session.comboCount >= 2) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color(0xFFFF6B35)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = "Combo",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "x${session.comboCount.coerceAtMost(3)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (session.dialogueHistory.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = NycGlassDark,
                        border = BorderStroke(1.dp, NycTeal.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { onToggleHistory() }
                            .testTag("toggle_dialogue_history")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Forum,
                                contentDescription = "Conversación",
                                tint = NycTeal,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "${session.dialogueHistory.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Live Vibe NYC bar
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NycGlassDark,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Vibe",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Box(
                            modifier = Modifier
                                .width(42.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedVibe.coerceIn(0.12f, 1f))
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Brush.horizontalGradient(listOf(NycTeal, NycAmber)))
                            )
                        }
                        Text(
                            text = "${session.nycVibePercent}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = NycAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NycLiveHistoryOverlayStrip(history: List<CompletedExchange>) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = NycGlassDark,
        border = BorderStroke(1.dp, NycTeal.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Conversación previa en esta escena",
                style = MaterialTheme.typography.labelSmall,
                color = NycTeal,
                fontWeight = FontWeight.Bold
            )
            history.forEachIndexed { idx, ex ->
                Text(
                    text = "${idx + 1}. Local: “${ex.partnerLineEn}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.84f)
                )
                Text(
                    text = "   Tú: “${ex.userLineEn}”",
                    style = MaterialTheme.typography.bodySmall,
                    color = NycAmber,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NycGlassVoiceOrbSection(
    session: PracticeSessionState,
    dictationState: DictationState,
    micPermissionDenied: Boolean,
    onUseMic: () -> Unit,
    onStopDictation: () -> Unit,
    onEditTranscript: () -> Unit,
    onUpdateTranscript: (String) -> Unit,
    onSelectAlternative: (String) -> Unit,
    onRetryDictation: () -> Unit,
    onSwitchToWrite: () -> Unit,
    onOpenVoiceSettings: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "glass_voice_pulse")
    val idleScale by pulse.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1150, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_scale"
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (micPermissionDenied) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xCC5F1A1A),
                border = BorderStroke(1.dp, Color(0xFFFF8A8A))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Sin permiso de micrófono. Puedes cambiar a Escribir en este mismo turno.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSwitchToWrite) {
                        Text("Escribir", color = NycAmber)
                    }
                }
            }
        }

        // Dramatic Glowing First-Person Voice Orb
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (dictationState.isListening) {
                val safeRms = if (dictationState.rmsLevel.isFinite()) dictationState.rmsLevel else 0.45f
                val liveBoost = safeRms.coerceIn(0.35f, 1.0f)
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(92.dp)) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(0.95f + liveBoost * 0.24f)
                            .clip(CircleShape)
                            .background(NycTeal.copy(alpha = 0.22f))
                    )
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .scale(0.96f + liveBoost * 0.14f)
                            .clip(CircleShape)
                            .background(NycTeal.copy(alpha = 0.34f))
                    )
                    Surface(
                        shape = CircleShape,
                        color = NycTeal,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .size(58.dp)
                            .clickable { onStopDictation() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = "Escuchando",
                                tint = Color(0xFF07131A),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                AnimatedAudioWave(
                    isActive = true,
                    barCount = 9,
                    amplitudeBoost = safeRms.coerceAtLeast(0.45f),
                    barColor = NycTeal,
                    height = 24.dp,
                    width = 94.dp
                )

                Text(
                    text = "Escuchando… habla en inglés",
                    style = MaterialTheme.typography.titleSmall,
                    color = NycTeal,
                    fontWeight = FontWeight.Bold
                )

                if (dictationState.partialTranscript.isNotBlank()) {
                    Text(
                        text = "“${dictationState.partialTranscript}”",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = onStopDictation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6B35),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("done_speaking_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Terminé de hablar", fontWeight = FontWeight.Bold)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .clickable { onUseMic() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("use_microphone_button"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .scale(idleScale)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(NycTeal.copy(alpha = 0.36f), Color.Transparent)
                                    )
                                )
                        )
                        Surface(
                            shape = CircleShape,
                            color = NycTeal,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = if (session.typedAnswer.isBlank()) {
                                        "Usar micrófono"
                                    } else {
                                        "Volver a dictar"
                                    },
                                    tint = Color(0xFF07131A),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (session.typedAnswer.isBlank()) {
                                "Usar micrófono"
                            } else {
                                "Volver a dictar"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Toca aquí y responde en voz alta",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.78f)
                        )
                    }
                }
            }
        }

        // Offline English pack download guidance
        if (dictationState.needsEnglishPackDownload) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xD93B2C0A),
                border = BorderStroke(1.dp, NycAmber.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Download,
                            contentDescription = null,
                            tint = NycAmber,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Descargar inglés para dictado",
                            style = MaterialTheme.typography.titleSmall,
                            color = NycAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Conéctate a internet para bajar el paquete en-US de Android, o cambia a Escribir y sigue en este mismo turno.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onOpenVoiceSettings,
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f))
                        ) {
                            Text("Ajustes de voz", color = Color.White)
                        }
                        Button(
                            onClick = onSwitchToWrite,
                            colors = ButtonDefaults.buttonColors(containerColor = NycTeal, contentColor = Color(0xFF07131A))
                        ) {
                            Text("Cambiar a Escribir", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (dictationState.errorMessageEs != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.10f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dictationState.errorMessageEs,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSwitchToWrite) {
                        Text("Escribir", color = NycTeal)
                    }
                }
            }
        }

        // Player Speech Bubble ("Revisa lo que se dictó")
        if (session.typedAnswer.isNotBlank() || session.isEditingTranscript) {
            Surface(
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 6.dp, bottomStart = 18.dp, bottomEnd = 18.dp),
                color = Color(0xCC0D2E35),
                border = BorderStroke(1.dp, NycTeal.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Revisa lo que se dictó",
                        style = MaterialTheme.typography.labelMedium,
                        color = NycTeal,
                        fontWeight = FontWeight.Bold
                    )

                    if (session.isEditingTranscript) {
                        OutlinedTextField(
                            value = session.typedAnswer,
                            onValueChange = onUpdateTranscript,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("edit_transcript_field"),
                            shape = RoundedCornerShape(14.dp),
                            label = { Text("Corrige el texto antes de enviar") },
                            colors = nycGlassTextFieldColors(),
                            minLines = 2
                        )
                    } else {
                        Text(
                            text = "“${session.typedAnswer}”",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (dictationState.alternatives.isNotEmpty()) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            dictationState.alternatives.forEach { alt ->
                                AssistChip(
                                    onClick = { onSelectAlternative(alt) },
                                    label = { Text(alt, color = Color.White) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color.White.copy(alpha = 0.12f)
                                    ),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (!session.isEditingTranscript) {
                            OutlinedButton(
                                onClick = onEditTranscript,
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                modifier = Modifier.testTag("edit_transcript_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Editar transcripción", color = Color.White)
                            }
                        }
                        TextButton(onClick = onRetryDictation) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = null,
                                tint = NycTeal,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Volver a dictar", color = NycTeal)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NycGlassWriteSection(
    typedAnswer: String,
    starterSparks: List<String>,
    onUpdateAnswerText: (String) -> Unit,
    onAppendSpark: (String) -> Unit,
    onSubmitAnswer: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (starterSparks.isNotEmpty()) {
            NycStarterSparksRow(
                label = "Chispas NYC · toca para construir rápido",
                sparks = starterSparks,
                onTapSpark = onAppendSpark
            )
        }

        OutlinedTextField(
            value = typedAnswer,
            onValueChange = onUpdateAnswerText,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("write_answer_input"),
            shape = RoundedCornerShape(16.dp),
            label = { Text("Responde en inglés a la escena") },
            placeholder = { Text("Ej.: Could I have a coffee, please?") },
            colors = nycGlassTextFieldColors(),
            minLines = 2,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(
                onSend = { if (typedAnswer.isNotBlank()) onSubmitAnswer() }
            )
        )
    }
}

@Composable
private fun nycGlassTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = NycTeal,
    focusedBorderColor = NycTeal,
    unfocusedBorderColor = Color.White.copy(alpha = 0.35f),
    focusedLabelColor = NycTeal,
    unfocusedLabelColor = Color.White.copy(alpha = 0.72f),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.5f),
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.45f),
    focusedContainerColor = Color(0x9907101C),
    unfocusedContainerColor = Color(0x8007101C)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NycStarterSparksRow(
    label: String,
    sparks: List<String>,
    onTapSpark: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = NycAmber,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.8f),
                fontWeight = FontWeight.SemiBold
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sparks.forEach { spark ->
                SuggestionChip(
                    onClick = { onTapSpark(spark) },
                    label = {
                        Text(
                            text = spark,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = Color.White.copy(alpha = 0.13f)
                    ),
                    border = BorderStroke(1.dp, NycAmber.copy(alpha = 0.45f))
                )
            }
        }
    }
}

@Composable
private fun NycGlassFeedbackCard(
    feedback: TurnFeedback,
    isOnline: Boolean,
    onPlayExample: () -> Unit,
    onConsultAiTutor: () -> Unit
) {
    val isPassed = feedback.outcome == EvaluationOutcome.PASSED
    val accent = when (feedback.outcome) {
        EvaluationOutcome.PASSED -> NycEmerald
        EvaluationOutcome.REVIEW_ANSWER -> NycAmber
        else -> Color(0xFFFF7B7B)
    }
    val bgColor = when (feedback.outcome) {
        EvaluationOutcome.PASSED -> Color(0xE00A2821)
        EvaluationOutcome.REVIEW_ANSWER -> Color(0xE02D220B)
        else -> Color(0xE0311216)
    }
    val heading = when (feedback.outcome) {
        EvaluationOutcome.PASSED -> "Objetivo cumplido"
        EvaluationOutcome.REVIEW_ANSWER -> "Revisa tu respuesta"
        else -> "Intenta otra vez"
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        border = BorderStroke(1.5.dp, accent.copy(alpha = 0.72f)),
        shadowElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("turn_feedback_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isPassed) Icons.Filled.CheckCircle else Icons.Filled.ErrorOutline,
                        contentDescription = heading,
                        tint = accent
                    )
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleMedium,
                        color = accent,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (feedback.source == EvaluationSource.AI_TUTOR) "Coach IA" else "Coach local",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Text(
                text = feedback.explanationEs,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.94f)
            )

            if (feedback.reusableTipEs.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.09f)
                ) {
                    Text(
                        text = "Tip callejero NYC: ${feedback.reusableTipEs}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
            }

            if (feedback.offlineTutorUnavailableNotice != null) {
                Text(
                    text = feedback.offlineTutorUnavailableNotice,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.11f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.16f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Frase modelo natural",
                            style = MaterialTheme.typography.labelSmall,
                            color = NycTeal
                        )
                        Text(
                            text = "“${feedback.modelExampleEn}”",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = onPlayExample,
                        modifier = Modifier.testTag("listen_example_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Escuchar ejemplo",
                            tint = NycTeal
                        )
                    }
                }
            }

            if (isOnline && feedback.source == EvaluationSource.LOCAL_OFFLINE && !isPassed) {
                TextButton(
                    onClick = onConsultAiTutor,
                    modifier = Modifier.testTag("ask_ai_tutor_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = NycAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Consultar otra opinión con el tutor IA", color = NycAmber)
                }
            }
        }
    }
}

@Composable
private fun NycGlassBottomActionBar(
    session: PracticeSessionState,
    isListening: Boolean,
    onSubmitAnswer: () -> Unit,
    onAdvanceOrFinish: () -> Unit,
    onEditAfterFeedback: () -> Unit,
    onPlayModelExample: (String) -> Unit
) {
    val feedback = session.feedback

    Surface(
        color = Color(0xEB070E18),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .widthIn(max = 680.dp)
                    .fillMaxWidth()
            ) {
                when {
                    feedback == null -> {
                        Button(
                            onClick = onSubmitAnswer,
                            enabled = session.typedAnswer.isNotBlank() &&
                                !isListening &&
                                !session.isConsultingAiTutor,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NycTeal,
                                contentColor = Color(0xFF07131A),
                                disabledContainerColor = Color.White.copy(alpha = 0.14f),
                                disabledContentColor = Color.White.copy(alpha = 0.45f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("send_answer_button"),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enviar respuesta", fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    feedback.outcome == EvaluationOutcome.PASSED -> {
                        Button(
                            onClick = onAdvanceOrFinish,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NycEmerald,
                                contentColor = Color(0xFF062016)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("next_or_finish_button"),
                            contentPadding = PaddingValues(vertical = 14.dp)
                        ) {
                            Text(
                                text = if (session.isLastTurn) "Terminar" else "Siguiente turno",
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = if (session.isLastTurn) {
                                    Icons.Filled.CheckCircle
                                } else {
                                    Icons.AutoMirrored.Filled.ArrowForward
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    else -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onPlayModelExample(feedback.modelExampleEn) },
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("listen_example_button"),
                                contentPadding = PaddingValues(vertical = 13.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = null,
                                    tint = NycTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Escuchar ejemplo", color = Color.White, maxLines = 1)
                            }
                            Button(
                                onClick = onEditAfterFeedback,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NycAmber,
                                    contentColor = Color(0xFF141005)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_my_answer_button"),
                                contentPadding = PaddingValues(vertical = 13.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Editar mi respuesta", fontWeight = FontWeight.ExtraBold, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}
