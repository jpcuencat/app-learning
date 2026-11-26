package com.example.speech

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

enum class NycAmbienceKind {
    CAFE,
    STREET,
    LOBBY
}

/**
 * Lightweight, 100% offline procedural NYC ambient soundscape using Android AudioTrack.
 * Generates soft warm filtered ambience and completely stops AudioTrack whenever the
 * microphone is active so it never competes with SpeechRecognizer / AudioRecord.
 */
class NycAmbiencePlayer {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val activeFlag = AtomicBoolean(false)
    private var playbackJob: Job? = null

    @Volatile
    private var isDucked: Boolean = false

    @Volatile
    private var isUserEnabled: Boolean = true

    @Volatile
    private var isPausedForMic: Boolean = false

    @Volatile
    private var currentKind: NycAmbienceKind = NycAmbienceKind.CAFE

    fun start(kind: NycAmbienceKind) {
        currentKind = kind
        if (!isUserEnabled || isPausedForMic) {
            stop()
            return
        }
        if (activeFlag.get() && playbackJob?.isActive == true) return

        stop()
        activeFlag.set(true)

        playbackJob = scope.launch {
            val sampleRate = 16_000
            val bufferSamples = 1024
            val minBuf = runCatching {
                AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
            }.getOrDefault(bufferSamples * 2).coerceAtLeast(bufferSamples * 2)

            val track = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            }.getOrNull() ?: run {
                activeFlag.set(false)
                return@launch
            }

            val pcm = ShortArray(bufferSamples)
            var brown = 0.0
            var lowPass = 0.0
            var phase = 0.0
            val rng = Random(42)

            try {
                track.play()
                while (isActive && activeFlag.get() && isUserEnabled && !isPausedForMic) {
                    val baseGain = when (kind) {
                        NycAmbienceKind.CAFE -> 0.040
                        NycAmbienceKind.STREET -> 0.034
                        NycAmbienceKind.LOBBY -> 0.026
                    }
                    val activeGain = if (isDucked) baseGain * 0.10 else baseGain
                    val smoothing = when (kind) {
                        NycAmbienceKind.CAFE -> 0.085
                        NycAmbienceKind.STREET -> 0.055
                        NycAmbienceKind.LOBBY -> 0.04
                    }

                    for (i in pcm.indices) {
                        val white = rng.nextDouble() * 2.0 - 1.0
                        brown = (brown + 0.025 * white) / 1.025
                        lowPass += smoothing * (brown - lowPass)
                        phase += (2.0 * PI * 0.14) / sampleRate
                        val swell = 0.78 + 0.22 * sin(phase)
                        val sample = (lowPass * 3.8 * swell * activeGain).coerceIn(-0.4, 0.4)
                        pcm[i] = (sample * Short.MAX_VALUE).toInt().toShort()
                    }
                    val written = track.write(pcm, 0, pcm.size)
                    if (written < 0) break
                }
            } catch (_: Throwable) {
                // Gracefully swallow any audio HAL transient errors
            } finally {
                runCatching {
                    track.pause()
                    track.flush()
                    track.stop()
                }
                runCatching { track.release() }
                activeFlag.set(false)
            }
        }
    }

    fun setDucked(ducked: Boolean) {
        isDucked = ducked
    }

    fun setPausedForMicrophone(paused: Boolean) {
        isPausedForMic = paused
        if (paused) {
            stop()
        } else if (isUserEnabled) {
            start(currentKind)
        }
    }

    fun setEnabled(enabled: Boolean, kind: NycAmbienceKind) {
        currentKind = kind
        isUserEnabled = enabled
        if (enabled && !isPausedForMic) {
            start(kind)
        } else {
            stop()
        }
    }

    fun stop() {
        activeFlag.set(false)
        playbackJob?.cancel()
        playbackJob = null
    }

    fun release() {
        stop()
        scope.cancel()
    }
}
