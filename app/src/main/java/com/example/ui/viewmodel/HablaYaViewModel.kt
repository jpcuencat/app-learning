package com.example.ui.viewmodel

import android.app.Application
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiTutorService
import com.example.ai.OnlineTutorService
import com.example.data.LessonCatalog
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferences
import com.example.data.local.UserPreferencesStore
import com.example.data.repository.CompletionResult
import com.example.data.repository.HablaYaRepository
import com.example.domain.LocalTurnEvaluator
import com.example.model.BlindVoiceSample
import com.example.model.BlitzRoundItem
import com.example.model.CefrLevel
import com.example.model.CharacterMood
import com.example.model.ConversationTurn
import com.example.model.ConversationVariant
import com.example.model.EvaluationOutcome
import com.example.model.EvaluationSource
import com.example.model.InputMode
import com.example.model.Lesson
import com.example.model.LessonProgressSummary
import com.example.model.SessionMode
import com.example.model.ThemeMode
import com.example.model.TurnFeedback
import com.example.model.VoiceProfile
import com.example.notifications.ReviewReminderScheduler
import com.example.speech.DictationController
import com.example.speech.DictationState
import com.example.speech.SpeechPlaybackState
import com.example.speech.VoiceCoachController
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompletedExchange(
    val partnerLineEn: String,
    val userLineEn: String,
    val reactionLineEn: String
)

data class PracticeSessionState(
    val lesson: Lesson,
    val mode: SessionMode,
    val turnIndex: Int,
    val inputMode: InputMode = InputMode.SPEAK,
    val typedAnswer: String = "",
    val isEditingTranscript: Boolean = false,
    val showHint: Boolean = false,
    val showPartnerTranslation: Boolean = false,
    val feedback: TurnFeedback? = null,
    val isConsultingAiTutor: Boolean = false,
    val completionResult: CompletionResult? = null,
    // Game HUD & Live NYC Stage metrics
    val comboCount: Int = 0,
    val maxCombo: Int = 0,
    val sessionXp: Int = 0,
    val nycVibePercent: Int = 30,
    val usedHintInSession: Boolean = false,
    val mistakesInSession: Int = 0,
    val hadMistakeOnCurrentTurn: Boolean = false,
    val characterMood: CharacterMood = CharacterMood.NEUTRAL,
    val characterReactionEs: String = "Te escucha atento",
    val activeReactionEn: String? = null,
    val dialogueHistory: List<CompletedExchange> = emptyList()
) {
    val variant: ConversationVariant
        get() = if (mode == SessionMode.PRACTICE) lesson.practice else lesson.review

    val currentTurn: ConversationTurn
        get() = variant.turns[turnIndex.coerceIn(0, variant.turns.lastIndex)]

    val totalTurns: Int
        get() = variant.turns.size

    val isLastTurn: Boolean
        get() = turnIndex >= variant.turns.lastIndex

    fun calculateStars(): Int {
        var stars = 1
        if (mistakesInSession <= 1) stars++
        if (!usedHintInSession && mistakesInSession == 0) stars++
        return stars.coerceIn(1, 3)
    }
}

data class BlitzGameState(
    val deck: List<BlitzRoundItem> = emptyList(),
    val currentIndex: Int = 0,
    val secondsLeft: Int = 60,
    val heartsLeft: Int = 3,
    val score: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val answeredCorrectly: Int = 0,
    val isFreeInputMode: Boolean = false,
    val freeInputText: String = "",
    val lastOutcomeCorrect: Boolean? = null,
    val lastFeedbackEs: String? = null,
    val timeBonusAwarded: Boolean = false,
    val isFinished: Boolean = false,
    val isNewRecord: Boolean = false,
    val xpRewarded: Int = 0
) {
    val currentItem: BlitzRoundItem?
        get() = if (deck.isEmpty()) null else deck[currentIndex % deck.size]
}

data class VoiceComparisonState(
    val samples: List<BlindVoiceSample> = emptyList(),
    val playedVoiceIds: Set<String> = emptySet(),
    val selectedVoiceId: String? = null,
    val isRevealed: Boolean = false
)

class HablaYaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HablaYaRepository(
        progressDao = AppDatabase.getInstance(application).lessonProgressDao(),
        preferencesStore = UserPreferencesStore(application)
    )
    private val aiTutor: OnlineTutorService = GeminiTutorService()
    private val voiceCoach = VoiceCoachController(application)
    private val dictationController = DictationController(application)

    val allLessons: List<Lesson> = repository.lessons

    val preferences: StateFlow<UserPreferences> = repository.preferencesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences()
    )

    val progressMap: StateFlow<Map<String, LessonProgressSummary>> =
        repository.progressMapFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    val pendingSyncCount: StateFlow<Int> = repository.pendingSyncCountFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0
    )

    val speechState: StateFlow<SpeechPlaybackState> = voiceCoach.state
    val dictationState: StateFlow<DictationState> = dictationController.state

    private val _selectedLevel = MutableStateFlow(CefrLevel.A1)
    val selectedLevel: StateFlow<CefrLevel> = _selectedLevel.asStateFlow()

    private val _sessionState = MutableStateFlow<PracticeSessionState?>(null)
    val sessionState: StateFlow<PracticeSessionState?> = _sessionState.asStateFlow()

    private val _blitzState = MutableStateFlow<BlitzGameState?>(null)
    val blitzState: StateFlow<BlitzGameState?> = _blitzState.asStateFlow()

    private val _voiceComparisonState = MutableStateFlow(createShuffledVoiceComparison())
    val voiceComparisonState: StateFlow<VoiceComparisonState> = _voiceComparisonState.asStateFlow()

    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private var hasEvaluatedInitialLock = false
    private var blitzTimerJob: Job? = null

    init {
        ReviewReminderScheduler.ensureChannel(application)
        viewModelScope.launch {
            preferences.collectLatest { prefs ->
                if (!hasEvaluatedInitialLock) {
                    hasEvaluatedInitialLock = true
                    if (prefs.biometricLockEnabled) {
                        _isUnlocked.value = false
                    }
                }
            }
        }
        viewModelScope.launch {
            dictationController.state.collectLatest { dState ->
                if (dState.isListening) {
                    _sessionState.update {
                        it?.copy(
                            characterMood = CharacterMood.LISTENING,
                            characterReactionEs = "Te está escuchando…"
                        )
                    }
                }
                if (dState.finalTranscript.isNotBlank()) {
                    _sessionState.update { session ->
                        if (session != null && session.inputMode == InputMode.SPEAK && !session.isEditingTranscript) {
                            session.copy(
                                typedAnswer = dState.finalTranscript,
                                characterMood = CharacterMood.NEUTRAL,
                                characterReactionEs = "Revisa tu frase y envíala"
                            )
                        } else {
                            session
                        }
                    }
                    _blitzState.update { blitz ->
                        if (blitz != null && blitz.isFreeInputMode && !blitz.isFinished) {
                            blitz.copy(freeInputText = dState.finalTranscript)
                        } else {
                            blitz
                        }
                    }
                }
            }
        }
    }

    fun unlockApp() {
        _isUnlocked.value = true
    }

    fun selectLevel(level: CefrLevel) {
        _selectedLevel.value = level
    }

    fun isDeviceOnline(): Boolean = runCatching {
        val cm = getApplication<Application>().getSystemService(ConnectivityManager::class.java)
            ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }.getOrDefault(false)

    fun currentVoiceProfile(): VoiceProfile =
        LessonCatalog.findVoiceProfile(preferences.value.selectedVoiceId)

    fun startSession(
        lessonId: String,
        mode: SessionMode,
        resumeSavedTurn: Boolean
    ) {
        closeBlitz()
        val lesson = LessonCatalog.findLesson(lessonId) ?: return
        val progress = progressMap.value[lessonId]
        val variant = if (mode == SessionMode.PRACTICE) lesson.practice else lesson.review
        val savedIndex = when (mode) {
            SessionMode.PRACTICE -> progress?.practiceTurnIndex ?: 0
            SessionMode.REVIEW -> progress?.reviewTurnIndex ?: 0
        }
        val initialTurn = if (resumeSavedTurn && savedIndex in variant.turns.indices) {
            savedIndex
        } else {
            0
        }

        voiceCoach.stop()
        dictationController.clearTranscript()
        _sessionState.value = PracticeSessionState(
            lesson = lesson,
            mode = mode,
            turnIndex = initialTurn,
            inputMode = InputMode.SPEAK,
            characterMood = CharacterMood.NEUTRAL,
            characterReactionEs = if (lesson.isBossMission) {
                "¡Imprevisto en curso! Responde con calma"
            } else {
                "Listo para conversar contigo"
            }
        )

        if (!resumeSavedTurn) {
            viewModelScope.launch {
                repository.saveTurnProgress(lessonId, mode, 0)
            }
        }

        // Bring the NYC scene alive by speaking the opening line if TTS is ready
        if (voiceCoach.state.value.isReady) {
            voiceCoach.speak(
                textEn = variant.turns[initialTurn].partnerLineEn,
                voiceProfile = currentVoiceProfile(),
                slow = false,
                tag = "partner_normal"
            )
        }
    }

    fun switchInputMode(mode: InputMode) {
        if (mode == InputMode.WRITE) {
            dictationController.stopListening()
        }
        _sessionState.update { it?.copy(inputMode = mode, isEditingTranscript = false) }
    }

    fun updateAnswerText(text: String) {
        _sessionState.update { it?.copy(typedAnswer = text) }
    }

    fun appendStarterSpark(spark: String) {
        val cleaned = spark.trim().removeSuffix("…").removeSuffix("...").trim()
        if (cleaned.isBlank()) return
        _sessionState.update { current ->
            if (current == null) return@update null
            val existing = current.typedAnswer.trimEnd()
            val combined = when {
                existing.isBlank() -> "$cleaned "
                existing.endsWith(" ") -> "$existing$cleaned "
                else -> "$existing $cleaned "
            }
            current.copy(
                typedAnswer = combined,
                inputMode = if (current.inputMode == InputMode.SPEAK && !current.isEditingTranscript) {
                    InputMode.WRITE
                } else {
                    current.inputMode
                }
            )
        }
    }

    fun toggleEditTranscript(editing: Boolean) {
        _sessionState.update { it?.copy(isEditingTranscript = editing) }
    }

    fun toggleHint() {
        _sessionState.update { current ->
            if (current == null) null
            else {
                val nextShow = !current.showHint
                current.copy(
                    showHint = nextShow,
                    usedHintInSession = current.usedHintInSession || nextShow
                )
            }
        }
    }

    fun togglePartnerTranslation() {
        _sessionState.update { it?.copy(showPartnerTranslation = !it.showPartnerTranslation) }
    }

    fun playPartnerPhrase(slow: Boolean) {
        val session = _sessionState.value ?: return
        voiceCoach.speak(
            textEn = session.currentTurn.partnerLineEn,
            voiceProfile = currentVoiceProfile(),
            slow = slow,
            tag = if (slow) "partner_slow" else "partner_normal"
        )
    }

    fun playModelExample(exampleText: String) {
        voiceCoach.speak(
            textEn = exampleText,
            voiceProfile = currentVoiceProfile(),
            slow = false,
            tag = "model_example"
        )
    }

    fun playCharacterReaction() {
        val reaction = _sessionState.value?.activeReactionEn ?: return
        voiceCoach.speak(
            textEn = reaction,
            voiceProfile = currentVoiceProfile(),
            slow = false,
            tag = "character_reaction"
        )
    }

    fun playPocketPhrase(phraseEn: String) {
        voiceCoach.speak(
            textEn = phraseEn,
            voiceProfile = currentVoiceProfile(),
            slow = false,
            tag = "pocket_phrase"
        )
    }

    fun stopAudio() {
        voiceCoach.stop()
    }

    fun isInlineSpeechSupported(): Boolean = dictationController.isInlineRecognitionSupported()

    fun buildSpeechRecognitionIntent(): android.content.Intent =
        dictationController.buildSpeechIntent(preferOffline = !isDeviceOnline())

    fun startDictation() {
        voiceCoach.stop()
        _sessionState.update { it?.copy(isEditingTranscript = false, feedback = null) }
        dictationController.startListening(preferOffline = !isDeviceOnline())
    }

    fun onExternalSpeechResults(matches: List<String>) {
        dictationController.acceptExternalResults(matches)
    }

    fun onExternalSpeechError(messageEs: String) {
        dictationController.reportError(messageEs)
    }

    fun stopDictation() {
        dictationController.stopListening()
    }

    fun selectDictationAlternative(alt: String) {
        dictationController.selectAlternative(alt)
        _sessionState.update { it?.copy(typedAnswer = alt) }
    }

    fun clearDictationAndRetry() {
        dictationController.clearTranscript()
        _sessionState.update { it?.copy(typedAnswer = "", isEditingTranscript = false, feedback = null) }
        startDictation()
    }

    fun submitAnswer() {
        val session = _sessionState.value ?: return
        val answerText = session.typedAnswer.trim()
        dictationController.stopListening()

        val localResult = LocalTurnEvaluator.evaluate(session.currentTurn, answerText)
        if (localResult.outcome != EvaluationOutcome.NEEDS_AI_TUTOR) {
            applyEvaluationFeedback(localResult)
            return
        }

        consultAiTutor(session, answerText)
    }

    fun requestAiTutorEvaluation() {
        val session = _sessionState.value ?: return
        val answerText = session.typedAnswer.trim()
        if (answerText.isBlank()) return
        consultAiTutor(session, answerText)
    }

    private fun applyEvaluationFeedback(feedback: TurnFeedback) {
        var reactionToSpeak: String? = null
        _sessionState.update { current ->
            if (current == null) return@update null
            when (feedback.outcome) {
                EvaluationOutcome.PASSED -> {
                    val nextCombo = if (current.hadMistakeOnCurrentTurn) 1 else current.comboCount + 1
                    val multiplier = nextCombo.coerceAtMost(3)
                    val baseTurnXp = if (current.lesson.isBossMission) 35 else 25
                    val gainedXp = baseTurnXp + (multiplier - 1) * 10
                    val nextVibe = (current.nycVibePercent + 24).coerceAtMost(100)
                    val reaction = when {
                        nextCombo >= 3 -> "¡Racha NYC x$nextCombo! Sonó como un local"
                        nextCombo == 2 -> "¡Combo x2! Le encantó tu respuesta"
                        else -> "¡Genial! Te respondió al instante"
                    }
                    reactionToSpeak = current.currentTurn.successReactionEn
                    current.copy(
                        feedback = feedback,
                        isConsultingAiTutor = false,
                        comboCount = nextCombo,
                        maxCombo = maxOf(current.maxCombo, nextCombo),
                        sessionXp = current.sessionXp + gainedXp,
                        nycVibePercent = nextVibe,
                        characterMood = CharacterMood.DELIGHTED,
                        characterReactionEs = reaction,
                        activeReactionEn = current.currentTurn.successReactionEn
                    )
                }

                EvaluationOutcome.REVIEW_ANSWER -> {
                    current.copy(
                        feedback = feedback,
                        isConsultingAiTutor = false,
                        comboCount = 0,
                        nycVibePercent = (current.nycVibePercent - 6).coerceAtLeast(15),
                        mistakesInSession = current.mistakesInSession + 1,
                        hadMistakeOnCurrentTurn = true,
                        characterMood = CharacterMood.ENCOURAGING,
                        characterReactionEs = "¡Casi! Un pequeño ajuste y la clavas",
                        activeReactionEn = null
                    )
                }

                else -> {
                    current.copy(
                        feedback = feedback,
                        isConsultingAiTutor = false,
                        comboCount = 0,
                        nycVibePercent = (current.nycVibePercent - 10).coerceAtLeast(15),
                        mistakesInSession = current.mistakesInSession + 1,
                        hadMistakeOnCurrentTurn = true,
                        characterMood = CharacterMood.CONFUSED,
                        characterReactionEs = "No te entendió bien · prueba otra vez",
                        activeReactionEn = null
                    )
                }
            }
        }
        reactionToSpeak?.takeIf { it.isNotBlank() }?.let { reply ->
            voiceCoach.speak(
                textEn = reply,
                voiceProfile = currentVoiceProfile(),
                slow = false,
                tag = "character_reaction"
            )
        }
    }

    private fun consultAiTutor(session: PracticeSessionState, answerText: String) {
        viewModelScope.launch {
            if (!isDeviceOnline()) {
                applyEvaluationFeedback(
                    TurnFeedback(
                        outcome = EvaluationOutcome.REVIEW_ANSWER,
                        explanationEs = "No pudimos evaluar esa variación sin conexión. Revisa la pista o el ejemplo para acercarte a la estructura esperada e inténtalo otra vez.",
                        reusableTipEs = session.currentTurn.hintEs,
                        modelExampleEn = session.currentTurn.modelExampleEn,
                        evaluatedText = answerText,
                        source = EvaluationSource.LOCAL_OFFLINE,
                        offlineTutorUnavailableNotice = "Sin conexión al tutor en línea en este momento."
                    )
                )
                return@launch
            }

            _sessionState.update {
                it?.copy(
                    isConsultingAiTutor = true,
                    feedback = null,
                    characterMood = CharacterMood.THINKING,
                    characterReactionEs = "Analizando tu frase…"
                )
            }
            val aiResult = aiTutor.evaluateResponse(
                lessonTitle = session.variant.titleEs,
                sceneGoal = session.variant.sceneGoalEs,
                turn = session.currentTurn,
                userAnswer = answerText
            )

            aiResult.fold(
                onSuccess = { feedback -> applyEvaluationFeedback(feedback) },
                onFailure = {
                    applyEvaluationFeedback(
                        TurnFeedback(
                            outcome = EvaluationOutcome.REVIEW_ANSWER,
                            explanationEs = "El tutor en línea no respondió en este instante, pero puedes apoyarte en la pista y la frase modelo para reintentar.",
                            reusableTipEs = session.currentTurn.hintEs,
                            modelExampleEn = session.currentTurn.modelExampleEn,
                            evaluatedText = answerText,
                            source = EvaluationSource.LOCAL_OFFLINE,
                            offlineTutorUnavailableNotice = "Tutor en línea temporalmente no disponible."
                        )
                    )
                }
            )
        }
    }

    fun editMyAnswerAfterFeedback() {
        _sessionState.update { current ->
            current?.copy(
                feedback = null,
                isEditingTranscript = current.inputMode == InputMode.SPEAK,
                characterMood = CharacterMood.NEUTRAL,
                characterReactionEs = "Ajusta tu frase y vuelve a enviar"
            )
        }
    }

    fun advanceTurnOrFinish() {
        val session = _sessionState.value ?: return
        voiceCoach.stop()
        dictationController.clearTranscript()

        if (!session.isLastTurn) {
            val nextIdx = session.turnIndex + 1
            val completedTurn = CompletedExchange(
                partnerLineEn = session.currentTurn.partnerLineEn,
                userLineEn = session.typedAnswer.trim(),
                reactionLineEn = session.currentTurn.successReactionEn
            )
            val nextTurn = session.variant.turns[nextIdx]
            _sessionState.value = session.copy(
                turnIndex = nextIdx,
                typedAnswer = "",
                isEditingTranscript = false,
                showHint = false,
                showPartnerTranslation = false,
                feedback = null,
                isConsultingAiTutor = false,
                hadMistakeOnCurrentTurn = false,
                characterMood = CharacterMood.NEUTRAL,
                characterReactionEs = "La escena sigue en NYC · escucha",
                activeReactionEn = null,
                dialogueHistory = session.dialogueHistory + completedTurn
            )
            viewModelScope.launch {
                repository.saveTurnProgress(session.lesson.id, session.mode, nextIdx)
            }
            if (voiceCoach.state.value.isReady) {
                voiceCoach.speak(
                    textEn = nextTurn.partnerLineEn,
                    voiceProfile = currentVoiceProfile(),
                    slow = false,
                    tag = "partner_normal"
                )
            }
        } else {
            val earnedStars = session.calculateStars()
            val totalSessionXp = session.sessionXp.coerceAtLeast(25) + earnedStars * 10
            viewModelScope.launch {
                val result = repository.completeSession(
                    lessonId = session.lesson.id,
                    mode = session.mode,
                    xpEarned = totalSessionXp,
                    starsEarned = earnedStars,
                    maxCombo = session.maxCombo
                )
                _sessionState.update { it?.copy(completionResult = result) }

                val prefs = preferences.value
                if (prefs.remindersEnabled) {
                    ReviewReminderScheduler.scheduleReviewReminder(
                        context = getApplication(),
                        lessonTitleEs = result.lesson.titleEs,
                        variantTitleEs = result.lesson.review.titleEs,
                        targetEpochDay = result.nextReviewEpochDay,
                        reminderHour = prefs.reminderHour
                    )
                }
            }
        }
    }

    fun closeSession() {
        voiceCoach.stop()
        dictationController.destroyRecognizer()
        _sessionState.value = null
    }

    // --- Mini-Game: Reto Relámpago (60s Arcade Blitz) ---

    fun startBlitzGame() {
        closeSession()
        blitzTimerJob?.cancel()
        val deck = LessonCatalog.buildBlitzDeck()
        _blitzState.value = BlitzGameState(
            deck = deck,
            currentIndex = 0,
            secondsLeft = 60,
            heartsLeft = 3,
            score = 0,
            combo = 0,
            maxCombo = 0
        )

        deck.firstOrNull()?.let { first ->
            voiceCoach.speak(
                textEn = first.partnerLineEn,
                voiceProfile = currentVoiceProfile(),
                slow = false,
                tag = "blitz_prompt"
            )
        }

        blitzTimerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                val current = _blitzState.value ?: break
                if (current.isFinished) break
                val nextSec = (current.secondsLeft - 1).coerceAtLeast(0)
                if (nextSec == 0) {
                    finishBlitz(current.copy(secondsLeft = 0))
                    break
                } else {
                    _blitzState.value = current.copy(secondsLeft = nextSec, timeBonusAwarded = false)
                }
            }
        }
    }

    fun playCurrentBlitzAudio() {
        val item = _blitzState.value?.currentItem ?: return
        voiceCoach.speak(
            textEn = item.partnerLineEn,
            voiceProfile = currentVoiceProfile(),
            slow = false,
            tag = "blitz_prompt"
        )
    }

    fun toggleBlitzFreeInput(enabled: Boolean) {
        dictationController.stopListening()
        _blitzState.update { it?.copy(isFreeInputMode = enabled, freeInputText = "") }
    }

    fun updateBlitzFreeText(text: String) {
        _blitzState.update { it?.copy(freeInputText = text) }
    }

    fun startBlitzDictation() {
        voiceCoach.stop()
        dictationController.startListening(preferOffline = !isDeviceOnline())
    }

    fun answerBlitzOption(selectedOptionEn: String) {
        val state = _blitzState.value ?: return
        if (state.isFinished) return
        val item = state.currentItem ?: return
        val isCorrect = selectedOptionEn == item.correctOptionEn
        processBlitzAnswer(state, isCorrect, isFreeBonus = false, explanation = item.explanationEs)
    }

    fun submitBlitzFreeAnswer() {
        val state = _blitzState.value ?: return
        if (state.isFinished) return
        val item = state.currentItem ?: return
        val text = state.freeInputText.trim()
        if (text.isBlank()) return
        dictationController.stopListening()
        val eval = LocalTurnEvaluator.evaluate(item.turnForFreeEvaluation, text)
        val isCorrect = eval.outcome == EvaluationOutcome.PASSED
        processBlitzAnswer(state, isCorrect, isFreeBonus = true, explanation = eval.explanationEs)
    }

    private fun processBlitzAnswer(
        state: BlitzGameState,
        isCorrect: Boolean,
        isFreeBonus: Boolean,
        explanation: String
    ) {
        if (isCorrect) {
            val nextCombo = state.combo + 1
            val mult = nextCombo.coerceAtMost(4)
            val basePts = if (isFreeBonus) 100 else 50
            val gained = basePts * mult
            val getsTimeBonus = nextCombo % 3 == 0
            val nextTime = if (getsTimeBonus) (state.secondsLeft + 3).coerceAtMost(60) else state.secondsLeft
            val nextIdx = state.currentIndex + 1

            val updated = state.copy(
                currentIndex = nextIdx,
                secondsLeft = nextTime,
                score = state.score + gained,
                combo = nextCombo,
                maxCombo = maxOf(state.maxCombo, nextCombo),
                answeredCorrectly = state.answeredCorrectly + 1,
                freeInputText = "",
                lastOutcomeCorrect = true,
                lastFeedbackEs = if (getsTimeBonus) "+$gained pts · ¡+3 s por racha x$nextCombo!" else "+$gained pts · ¡Acertaste!",
                timeBonusAwarded = getsTimeBonus
            )
            _blitzState.value = updated
            updated.currentItem?.let { nextItem ->
                voiceCoach.speak(nextItem.partnerLineEn, currentVoiceProfile(), false, "blitz_prompt")
            }
        } else {
            val nextHearts = (state.heartsLeft - 1).coerceAtLeast(0)
            val nextState = state.copy(
                currentIndex = state.currentIndex + 1,
                heartsLeft = nextHearts,
                combo = 0,
                freeInputText = "",
                lastOutcomeCorrect = false,
                lastFeedbackEs = "Ups · Era: \"${state.currentItem?.correctOptionEn.orEmpty()}\"",
                timeBonusAwarded = false
            )
            if (nextHearts == 0) {
                finishBlitz(nextState)
            } else {
                _blitzState.value = nextState
                nextState.currentItem?.let { nextItem ->
                    voiceCoach.speak(nextItem.partnerLineEn, currentVoiceProfile(), false, "blitz_prompt")
                }
            }
        }
    }

    private fun finishBlitz(finalState: BlitzGameState) {
        blitzTimerJob?.cancel()
        voiceCoach.stop()
        dictationController.stopListening()
        val prevBest = preferences.value.blitzBestScore
        val isNewRecord = finalState.score > prevBest && finalState.score > 0
        val xpReward = (finalState.score / 10).coerceAtLeast(15)
        _blitzState.value = finalState.copy(
            isFinished = true,
            isNewRecord = isNewRecord,
            xpRewarded = xpReward
        )
        viewModelScope.launch {
            repository.recordBlitzCompletion(score = finalState.score, xpEarned = xpReward)
        }
    }

    fun closeBlitz() {
        blitzTimerJob?.cancel()
        voiceCoach.stop()
        dictationController.destroyRecognizer()
        _blitzState.value = null
    }

    // --- Voice Comparison (Blind A / B / C) ---

    fun resetVoiceComparison() {
        voiceCoach.stop()
        _voiceComparisonState.value = createShuffledVoiceComparison()
    }

    fun playBlindVoiceSample(sample: BlindVoiceSample) {
        voiceCoach.speak(
            textEn = LessonCatalog.VOICE_COMPARISON_SAMPLE_EN,
            voiceProfile = sample.profile,
            slow = false,
            tag = "voice_${sample.profile.id}"
        )
        _voiceComparisonState.update { state ->
            state.copy(playedVoiceIds = state.playedVoiceIds + sample.profile.id)
        }
    }

    fun chooseFavoriteVoice(sample: BlindVoiceSample) {
        voiceCoach.stop()
        _voiceComparisonState.update { state ->
            state.copy(
                selectedVoiceId = sample.profile.id,
                isRevealed = true
            )
        }
        viewModelScope.launch {
            repository.setSelectedVoice(sample.profile.id, fromComparison = true)
        }
    }

    // --- Settings ---

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setBiometricLockEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setBiometricLockEnabled(enabled) }
    }

    fun setRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.setRemindersEnabled(enabled)
            if (!enabled) {
                ReviewReminderScheduler.cancelReminders(getApplication())
            }
        }
    }

    fun sendTestReminderNotification(): Boolean {
        return ReviewReminderScheduler.showImmediateNotification(
            context = getApplication(),
            title = "Recordatorio de HablaYa",
            body = "Tu racha te espera: repasa una misión de inglés en 2 minutos."
        )
    }

    fun clearAllProgress() {
        viewModelScope.launch { repository.clearAllProgress() }
    }

    override fun onCleared() {
        super.onCleared()
        blitzTimerJob?.cancel()
        voiceCoach.shutdown()
        dictationController.destroyRecognizer()
    }

    companion object {
        fun createShuffledVoiceComparison(
            profiles: List<VoiceProfile> = LessonCatalog.voiceProfiles
        ): VoiceComparisonState {
            val labels = listOf("Voz A", "Voz B", "Voz C")
            val shuffled = profiles.shuffled()
            val samples = shuffled.mapIndexed { idx, profile ->
                BlindVoiceSample(
                    blindLabel = labels.getOrElse(idx) { "Voz ${idx + 1}" },
                    profile = profile
                )
            }
            return VoiceComparisonState(samples = samples)
        }
    }
}
