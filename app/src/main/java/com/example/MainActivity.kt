package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ai.configureAppCheck
import com.example.data.LessonCatalog
import com.example.model.SessionMode
import com.example.security.BiometricAuthHelper
import com.example.ui.screens.BiometricLockOverlay
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LessonSummaryScreen
import com.example.ui.screens.LightningBlitzScreen
import com.example.ui.screens.PracticeTurnScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VoiceComparisonScreen
import com.example.ui.theme.HablaYaTheme
import com.example.ui.viewmodel.HablaYaViewModel

private enum class TopDestination(
    val route: String,
    val labelEs: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("home", "Mapa", Icons.Filled.Explore, Icons.Outlined.Explore),
    VOICES("voices", "Voces", Icons.Filled.GraphicEq, Icons.Outlined.GraphicEq),
    SETTINGS("settings", "Ajustes", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure Firebase App Check with launch intent before UI or ViewModels.
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            val viewModel: HablaYaViewModel = viewModel()
            val preferences by viewModel.preferences.collectAsStateWithLifecycle()
            val progressMap by viewModel.progressMap.collectAsStateWithLifecycle()
            val selectedLevel by viewModel.selectedLevel.collectAsStateWithLifecycle()
            val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
            val blitzState by viewModel.blitzState.collectAsStateWithLifecycle()
            val speechState by viewModel.speechState.collectAsStateWithLifecycle()
            val dictationState by viewModel.dictationState.collectAsStateWithLifecycle()
            val voiceComparisonState by viewModel.voiceComparisonState.collectAsStateWithLifecycle()
            val pendingSyncCount by viewModel.pendingSyncCount.collectAsStateWithLifecycle()
            val isUnlocked by viewModel.isUnlocked.collectAsStateWithLifecycle()

            var biometricError by remember { mutableStateOf<String?>(null) }

            val triggerBiometricPrompt = {
                BiometricAuthHelper.authenticate(
                    activity = this@MainActivity,
                    onSuccess = {
                        biometricError = null
                        viewModel.unlockApp()
                    },
                    onError = { err -> biometricError = err }
                )
            }

            LaunchedEffect(isUnlocked) {
                if (!isUnlocked) {
                    triggerBiometricPrompt()
                }
            }

            HablaYaTheme(themeMode = preferences.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (!isUnlocked) {
                        BiometricLockOverlay(
                            errorMessage = biometricError,
                            onRequestAuth = triggerBiometricPrompt
                        )
                    } else {
                        val activeBlitz = blitzState
                        val activeSession = sessionState
                        when {
                            activeBlitz != null -> {
                                LightningBlitzScreen(
                                    state = activeBlitz,
                                    bestScore = preferences.blitzBestScore,
                                    dictationState = dictationState,
                                    onClose = { viewModel.closeBlitz() },
                                    onPlayAgain = { viewModel.startBlitzGame() },
                                    onPlayAudio = { viewModel.playCurrentBlitzAudio() },
                                    onSelectOption = { viewModel.answerBlitzOption(it) },
                                    onToggleFreeMode = { viewModel.toggleBlitzFreeInput(it) },
                                    onUpdateFreeText = { viewModel.updateBlitzFreeText(it) },
                                    onStartDictation = { viewModel.startBlitzDictation() },
                                    onSubmitFreeAnswer = { viewModel.submitBlitzFreeAnswer() }
                                )
                            }

                            activeSession != null && activeSession.completionResult != null -> {
                                LessonSummaryScreen(
                                    result = activeSession.completionResult,
                                    onPlayPhrase = { viewModel.playPocketPhrase(it) },
                                    onTryVariant = {
                                        viewModel.startSession(
                                            lessonId = activeSession.lesson.id,
                                            mode = SessionMode.REVIEW,
                                            resumeSavedTurn = false
                                        )
                                    },
                                    onReturnHome = { viewModel.closeSession() }
                                )
                            }

                            activeSession != null -> {
                                PracticeTurnScreen(
                                    session = activeSession,
                                    speechState = speechState,
                                    dictationState = dictationState,
                                    isOnline = viewModel.isDeviceOnline(),
                                    onBack = { viewModel.closeSession() },
                                    onPlayPartnerPhrase = { slow -> viewModel.playPartnerPhrase(slow) },
                                    onPlayReaction = { viewModel.playCharacterReaction() },
                                    onPlayModelExample = { ex -> viewModel.playModelExample(ex) },
                                    onSwitchInputMode = { mode -> viewModel.switchInputMode(mode) },
                                    onUpdateAnswerText = { text -> viewModel.updateAnswerText(text) },
                                    onAppendSpark = { spark -> viewModel.appendStarterSpark(spark) },
                                    onToggleEditTranscript = { edit -> viewModel.toggleEditTranscript(edit) },
                                    onToggleHint = { viewModel.toggleHint() },
                                    onTogglePartnerTranslation = { viewModel.togglePartnerTranslation() },
                                    onStartDictation = { viewModel.startDictation() },
                                    onStopDictation = { viewModel.stopDictation() },
                                    isInlineSpeechSupported = viewModel.isInlineSpeechSupported(),
                                    buildSpeechIntent = { viewModel.buildSpeechRecognitionIntent() },
                                    onExternalSpeechResults = { matches -> viewModel.onExternalSpeechResults(matches) },
                                    onExternalSpeechError = { msg -> viewModel.onExternalSpeechError(msg) },
                                    onSelectAlternative = { alt -> viewModel.selectDictationAlternative(alt) },
                                    onRetryDictation = { viewModel.clearDictationAndRetry() },
                                    onSubmitAnswer = { viewModel.submitAnswer() },
                                    onConsultAiTutor = { viewModel.requestAiTutorEvaluation() },
                                    onEditAfterFeedback = { viewModel.editMyAnswerAfterFeedback() },
                                    onAdvanceOrFinish = { viewModel.advanceTurnOrFinish() }
                                )
                            }

                            else -> {
                                MainTabbedShell(
                                    viewModel = viewModel,
                                    preferences = preferences,
                                    progressMap = progressMap,
                                    selectedLevel = selectedLevel,
                                    speechState = speechState,
                                    voiceComparisonState = voiceComparisonState,
                                    pendingSyncCount = pendingSyncCount
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MainTabbedShell(
    viewModel: HablaYaViewModel,
    preferences: com.example.data.local.UserPreferences,
    progressMap: Map<String, com.example.model.LessonProgressSummary>,
    selectedLevel: com.example.model.CefrLevel,
    speechState: com.example.speech.SpeechPlaybackState,
    voiceComparisonState: com.example.ui.viewmodel.VoiceComparisonState,
    pendingSyncCount: Int
) {
    var currentTab by remember { mutableStateOf(TopDestination.HOME) }
    val currentVoice = LessonCatalog.findVoiceProfile(preferences.selectedVoiceId)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 700.dp

        if (useNavRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                NavigationRail(
                    modifier = Modifier.statusBarsPadding()
                ) {
                    TopDestination.entries.forEach { dest ->
                        val selected = currentTab == dest
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                if (dest == TopDestination.VOICES && currentTab != TopDestination.VOICES) {
                                    viewModel.resetVoiceComparison()
                                }
                                currentTab = dest
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.labelEs
                                )
                            },
                            label = { Text(dest.labelEs) },
                            modifier = Modifier.testTag("nav_${dest.route}")
                        )
                    }
                }

                TabContentHost(
                    currentTab = currentTab,
                    viewModel = viewModel,
                    preferences = preferences,
                    progressMap = progressMap,
                    selectedLevel = selectedLevel,
                    currentVoice = currentVoice,
                    speechState = speechState,
                    voiceComparisonState = voiceComparisonState,
                    pendingSyncCount = pendingSyncCount,
                    onNavigateToVoices = {
                        viewModel.resetVoiceComparison()
                        currentTab = TopDestination.VOICES
                    },
                    modifier = Modifier
                        .weight(1f)
                        .statusBarsPadding()
                )
            }
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    NavigationBar {
                        TopDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (dest == TopDestination.VOICES && currentTab != TopDestination.VOICES) {
                                        viewModel.resetVoiceComparison()
                                    }
                                    currentTab = dest
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = dest.labelEs
                                    )
                                },
                                label = { Text(dest.labelEs) },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                TabContentHost(
                    currentTab = currentTab,
                    viewModel = viewModel,
                    preferences = preferences,
                    progressMap = progressMap,
                    selectedLevel = selectedLevel,
                    currentVoice = currentVoice,
                    speechState = speechState,
                    voiceComparisonState = voiceComparisonState,
                    pendingSyncCount = pendingSyncCount,
                    onNavigateToVoices = {
                        viewModel.resetVoiceComparison()
                        currentTab = TopDestination.VOICES
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .statusBarsPadding()
                )
            }
        }
    }
}

@Composable
private fun TabContentHost(
    currentTab: TopDestination,
    viewModel: HablaYaViewModel,
    preferences: com.example.data.local.UserPreferences,
    progressMap: Map<String, com.example.model.LessonProgressSummary>,
    selectedLevel: com.example.model.CefrLevel,
    currentVoice: com.example.model.VoiceProfile,
    speechState: com.example.speech.SpeechPlaybackState,
    voiceComparisonState: com.example.ui.viewmodel.VoiceComparisonState,
    pendingSyncCount: Int,
    onNavigateToVoices: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (currentTab) {
        TopDestination.HOME -> HomeScreen(
            lessons = viewModel.allLessons,
            selectedLevel = selectedLevel,
            progressMap = progressMap,
            preferences = preferences,
            currentVoice = currentVoice,
            isOnline = viewModel.isDeviceOnline(),
            onSelectLevel = { viewModel.selectLevel(it) },
            onOpenLesson = { id, mode, resume -> viewModel.startSession(id, mode, resume) },
            onStartBlitz = { viewModel.startBlitzGame() },
            onPlayPocketPhrase = { viewModel.playPocketPhrase(it) },
            onOpenVoiceComparison = onNavigateToVoices,
            modifier = modifier
        )

        TopDestination.VOICES -> VoiceComparisonScreen(
            comparisonState = voiceComparisonState,
            speechState = speechState,
            activeDefaultVoiceId = preferences.selectedVoiceId,
            onPlaySample = { viewModel.playBlindVoiceSample(it) },
            onStopAudio = { viewModel.stopAudio() },
            onChooseFavorite = { viewModel.chooseFavoriteVoice(it) },
            onReshuffle = { viewModel.resetVoiceComparison() },
            modifier = modifier
        )

        TopDestination.SETTINGS -> SettingsScreen(
            preferences = preferences,
            currentVoice = currentVoice,
            pendingSyncCount = pendingSyncCount,
            onSelectThemeMode = { viewModel.setThemeMode(it) },
            onToggleBiometric = { viewModel.setBiometricLockEnabled(it) },
            onToggleReminders = { viewModel.setRemindersEnabled(it) },
            onTestNotification = { viewModel.sendTestReminderNotification() },
            onOpenVoiceComparison = onNavigateToVoices,
            onClearProgress = { viewModel.clearAllProgress() },
            modifier = modifier
        )
    }
}
