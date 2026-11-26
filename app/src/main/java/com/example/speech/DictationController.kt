package com.example.speech

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class DictationState(
    val isAvailable: Boolean = true,
    val isListening: Boolean = false,
    val partialTranscript: String = "",
    val finalTranscript: String = "",
    val alternatives: List<String> = emptyList(),
    val rmsLevel: Float = 0f,
    val needsEnglishPackDownload: Boolean = false,
    val errorMessageEs: String? = null
)

/**
 * Crash-safe Android SpeechRecognizer controller for English dictation.
 * - Executes all SpeechRecognizer API calls strictly on the Main Looper.
 * - Guards against missing RECORD_AUDIO permission, unavailable RecognitionService,
 *   NaN/Infinity RMS values, and binder/runtime exceptions.
 * - Supports Android 12+ on-device recognition fallback and external RecognizerIntent results.
 */
class DictationController(private val context: Context) {

    private val appContext: Context = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(
        DictationState(isAvailable = isInlineRecognitionSupported())
    )
    val state: StateFlow<DictationState> = _state.asStateFlow()

    private var recognizer: SpeechRecognizer? = null

    fun isInlineRecognitionSupported(): Boolean {
        val standard = runCatching { SpeechRecognizer.isRecognitionAvailable(appContext) }
            .getOrDefault(false)
        val onDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext) }
                .getOrDefault(false)
        } else {
            false
        }
        return standard || onDevice
    }

    private fun runOnMain(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            mainHandler.post(block)
        }
    }

    fun startListening(preferOffline: Boolean = false) {
        runOnMain {
            val hasPerm = runCatching {
                ContextCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)

            if (!hasPerm) {
                _state.value = _state.value.copy(
                    isListening = false,
                    rmsLevel = 0f,
                    errorMessageEs = "Necesitamos permiso de micrófono para dictar en inglés. También puedes cambiar a Escribir."
                )
                return@runOnMain
            }

            val standardAvail = runCatching {
                SpeechRecognizer.isRecognitionAvailable(appContext)
            }.getOrDefault(false)
            val onDeviceAvail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                runCatching { SpeechRecognizer.isOnDeviceRecognitionAvailable(appContext) }
                    .getOrDefault(false)
            } else {
                false
            }

            if (!standardAvail && !onDeviceAvail) {
                _state.value = _state.value.copy(
                    isAvailable = false,
                    isListening = false,
                    rmsLevel = 0f,
                    errorMessageEs = "El servicio de dictado por voz no está disponible en este dispositivo. Puedes usar el modo Escribir en este mismo turno."
                )
                return@runOnMain
            }

            destroyRecognizerInternal()

            val created = runCatching {
                val sr = if ((preferOffline || !standardAvail) && onDeviceAvail && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(appContext)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(appContext)
                }
                recognizer = sr
                sr.setRecognitionListener(createListener())

                val intent = buildSpeechIntent(preferOffline)
                _state.value = _state.value.copy(
                    isAvailable = true,
                    isListening = true,
                    rmsLevel = 0.25f,
                    errorMessageEs = null,
                    needsEnglishPackDownload = false,
                    partialTranscript = ""
                )
                sr.startListening(intent)
            }

            created.onFailure {
                destroyRecognizerInternal()
                _state.value = _state.value.copy(
                    isListening = false,
                    rmsLevel = 0f,
                    errorMessageEs = "No se pudo activar el micrófono en este dispositivo. Puedes cambiar a Escribir y continuar en este mismo turno."
                )
            }
        }
    }

    fun buildSpeechIntent(preferOffline: Boolean = false): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, appContext.packageName)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 4)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla en inglés para responder en NYC")
            if (preferOffline) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
        }
    }

    fun acceptExternalResults(matches: List<String>) {
        val cleaned = matches.map { it.trim() }.filter { it.isNotBlank() }.distinct()
        val best = cleaned.firstOrNull().orEmpty()
        val others = cleaned.drop(1).take(3)
        _state.value = _state.value.copy(
            isListening = false,
            rmsLevel = 0f,
            partialTranscript = "",
            finalTranscript = best,
            alternatives = others,
            errorMessageEs = if (best.isBlank()) {
                "No se reconoció ninguna frase en inglés. Intenta otra vez o cambia a Escribir."
            } else null
        )
    }

    fun reportError(messageEs: String) {
        _state.value = _state.value.copy(
            isListening = false,
            rmsLevel = 0f,
            errorMessageEs = messageEs
        )
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = _state.value.copy(
                isListening = true,
                errorMessageEs = null,
                needsEnglishPackDownload = false,
                partialTranscript = ""
            )
        }

        override fun onBeginningOfSpeech() {
            _state.value = _state.value.copy(isListening = true)
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Guard against NaN / +/-Infinity from emulator or HAL microphones
            val safeDb = if (rmsdB.isFinite()) rmsdB else 0f
            val normalized = ((safeDb + 2f) / 12f).coerceIn(0.08f, 1f)
            _state.value = _state.value.copy(rmsLevel = normalized)
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            _state.value = _state.value.copy(isListening = false, rmsLevel = 0f)
        }

        override fun onError(error: Int) {
            val needsDownload = error == SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED ||
                error == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ||
                error == SpeechRecognizer.ERROR_NETWORK ||
                error == SpeechRecognizer.ERROR_NETWORK_TIMEOUT

            val message = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH ->
                    "No se entendió ninguna frase en inglés. Intenta hablar un poco más cerca del micrófono o cambia a Escribir."
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                    "No detectamos voz. Toca Usar micrófono para intentar de nuevo."
                SpeechRecognizer.ERROR_AUDIO ->
                    "Hubo un problema al capturar el audio del micrófono. Puedes volver a intentar o cambiar a Escribir."
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                    "Necesitamos permiso de micrófono para dictar en inglés."
                SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
                SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE ->
                    "Falta el paquete de inglés para dictado en el teléfono."
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                    "No se pudo transcribir sin conexión. Descarga el paquete de inglés de Android o cambia a Escribir."
                else ->
                    "No se pudo completar el dictado en este momento. Puedes volver a dictar o cambiar a Escribir."
            }

            _state.value = _state.value.copy(
                isListening = false,
                rmsLevel = 0f,
                needsEnglishPackDownload = needsDownload,
                errorMessageEs = message
            )
        }

        override fun onResults(results: Bundle?) {
            val matches = runCatching {
                results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    ?.distinct()
                    .orEmpty()
            }.getOrDefault(emptyList())

            acceptExternalResults(matches)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = runCatching {
                partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
            }.getOrDefault("")
            if (partial.isNotBlank()) {
                _state.value = _state.value.copy(partialTranscript = partial)
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    fun stopListening() {
        runOnMain {
            runCatching { recognizer?.stopListening() }
            _state.value = _state.value.copy(isListening = false, rmsLevel = 0f)
        }
    }

    fun clearTranscript() {
        _state.value = _state.value.copy(
            partialTranscript = "",
            finalTranscript = "",
            alternatives = emptyList(),
            errorMessageEs = null,
            needsEnglishPackDownload = false
        )
    }

    fun selectAlternative(alternative: String) {
        val currentMain = _state.value.finalTranscript
        val updatedAlts = buildList {
            if (currentMain.isNotBlank() && currentMain != alternative) add(currentMain)
            addAll(_state.value.alternatives.filter { it != alternative })
        }
        _state.value = _state.value.copy(
            finalTranscript = alternative,
            alternatives = updatedAlts
        )
    }

    fun destroyRecognizer() {
        runOnMain {
            destroyRecognizerInternal()
        }
    }

    private fun destroyRecognizerInternal() {
        runCatching {
            recognizer?.cancel()
            recognizer?.destroy()
        }
        recognizer = null
        _state.value = _state.value.copy(isListening = false, rmsLevel = 0f)
    }
}
