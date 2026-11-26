package com.example.speech

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.example.data.LessonCatalog
import com.example.model.VoiceProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

data class SpeechPlaybackState(
    val isReady: Boolean = false,
    val activeTag: String? = null,
    val isSpeaking: Boolean = false,
    val isSlowMode: Boolean = false,
    val errorMessageEs: String? = null
)

/**
 * Real Android TextToSpeech controller supporting offline English speech synthesis,
 * 75% slower playback preserving pitch, and distinct calibrated voice profiles.
 */
class VoiceCoachController(context: Context) {

    private val _state = MutableStateFlow(SpeechPlaybackState())
    val state: StateFlow<SpeechPlaybackState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null
    private var availableVoices: List<Voice> = emptyList()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val engine = tts
                engine?.language = Locale.US
                availableVoices = runCatching {
                    engine?.voices?.filter { it.locale.language == "en" }?.toList().orEmpty()
                }.getOrDefault(emptyList())

                engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _state.value = _state.value.copy(isSpeaking = true, errorMessageEs = null)
                    }

                    override fun onDone(utteranceId: String?) {
                        _state.value = _state.value.copy(isSpeaking = false, activeTag = null)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.value = _state.value.copy(
                            isSpeaking = false,
                            activeTag = null,
                            errorMessageEs = "No se pudo reproducir el audio en este dispositivo."
                        )
                    }

                    override fun onError(utteranceId: String?, errorCode: Int) {
                        _state.value = _state.value.copy(
                            isSpeaking = false,
                            activeTag = null,
                            errorMessageEs = "No se pudo reproducir el audio (código $errorCode)."
                        )
                    }
                })
                _state.value = _state.value.copy(isReady = true)
            } else {
                _state.value = _state.value.copy(
                    isReady = false,
                    errorMessageEs = "El motor de voz en inglés no está disponible todavía."
                )
            }
        }
    }

    fun speak(
        textEn: String,
        voiceProfile: VoiceProfile = LessonCatalog.voiceProfiles.first(),
        slow: Boolean = false,
        tag: String = "turn_prompt"
    ) {
        val engine = tts
        if (engine == null || !_state.value.isReady) {
            _state.value = _state.value.copy(
                errorMessageEs = "Preparando voz en inglés, intenta de nuevo en un segundo."
            )
            return
        }

        // Apply voice profile (best matching installed English voice + calibrated pitch)
        val matchedVoice = selectBestAndroidVoice(voiceProfile)
        if (matchedVoice != null) {
            engine.voice = matchedVoice
        } else {
            val langResult = engine.setLanguage(voiceProfile.locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.setLanguage(Locale.US)
            }
        }

        // Preserve pitch when playing at 75% slower speed!
        engine.setPitch(voiceProfile.pitch)
        val rate = if (slow) (voiceProfile.baseRate * 0.75f).coerceAtLeast(0.55f) else voiceProfile.baseRate
        engine.setSpeechRate(rate)

        val utteranceId = "${tag}_${UUID.randomUUID()}"
        _state.value = _state.value.copy(
            activeTag = tag,
            isSpeaking = true,
            isSlowMode = slow,
            errorMessageEs = null
        )

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        engine.speak(textEn, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    private fun selectBestAndroidVoice(profile: VoiceProfile): Voice? {
        if (availableVoices.isEmpty()) return null
        // 1. Prefer offline/installed matching keyword
        for (keyword in profile.preferredVoiceNameKeywords) {
            val match = availableVoices.firstOrNull { voice ->
                voice.name.contains(keyword, ignoreCase = true) && !voice.isNetworkConnectionRequired
            } ?: availableVoices.firstOrNull { voice ->
                voice.name.contains(keyword, ignoreCase = true)
            }
            if (match != null) return match
        }
        // 2. Match country (US vs UK)
        val countryMatch = availableVoices.firstOrNull {
            it.locale.country.equals(profile.locale.country, ignoreCase = true) &&
                !it.isNetworkConnectionRequired
        }
        if (countryMatch != null) return countryMatch

        // 3. Deterministic spread across installed English voices so A/B/C sound distinct
        val profileIndex = LessonCatalog.voiceProfiles.indexOfFirst { it.id == profile.id }.coerceAtLeast(0)
        return availableVoices[profileIndex % availableVoices.size]
    }

    fun stop() {
        runCatching { tts?.stop() }
        _state.value = _state.value.copy(isSpeaking = false, activeTag = null)
    }

    fun shutdown() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
    }
}
