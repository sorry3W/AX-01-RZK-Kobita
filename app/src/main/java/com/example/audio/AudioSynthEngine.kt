package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * Real-time DSP Software Synthesizer Engine for AX-100 AI Z File Device.
 * Generates low-latency PCM audio output via AudioTrack.
 */
class AudioSynthEngine {

    companion object {
        const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 1024
    }

    enum class OscillatorType {
        SINE, SAWTOOTH, SQUARE, TRIANGLE, NOISE, WAVETABLE, FM, GRANULAR
    }

    enum class LfoTarget {
        CUTOFF, PITCH, AMP
    }

    // Active Polyphonic Notes: Map of MIDI note number -> NoteState
    data class NoteState(
        val midiNote: Int,
        val frequency: Float,
        var velocity: Float,
        var ageSamples: Long = 0,
        var isReleasing: Boolean = false,
        var releaseSamples: Long = 0
    )

    // Current Synth Parameters
    var oscillatorType: OscillatorType = OscillatorType.SAWTOOTH
    var attackMs: Float = 30f
    var decayMs: Float = 150f
    var sustainLevel: Float = 0.7f
    var releaseMs: Float = 250f

    var filterCutoffHz: Float = 2500f
    var filterResonance: Float = 0.3f
    var lfoRateHz: Float = 2f
    var lfoDepth: Float = 0.3f
    var lfoTarget: LfoTarget = LfoTarget.CUTOFF

    var reverbLevel: Float = 0.25f
    var delayLevel: Float = 0.20f
    var distortionLevel: Float = 0.0f
    var masterVolume: Float = 0.8f

    // Internal AudioTrack
    private var audioTrack: AudioTrack? = null
    private var isEngineRunning = false
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    // Active notes list
    private val activeNotes = mutableListOf<NoteState>()

    // LFO phase tracker
    private var lfoPhase = 0.0

    // Filter state (1-pole / 2-pole simple RC model)
    private var filterLowPassState = 0.0f

    // Reverb / Delay Circular Buffers
    private val delayBufferLength = (SAMPLE_RATE * 0.3).toInt() // 300ms delay
    private val delayBuffer = FloatArray(delayBufferLength)
    private var delayWritePos = 0

    // Live Waveform Buffer for UI Visualizer (256 floats in range -1.0..1.0)
    private val _visualizerBuffer = MutableStateFlow(FloatArray(256))
    val visualizerBuffer: StateFlow<FloatArray> = _visualizerBuffer.asStateFlow()

    // Status Flow
    private val _isAudioActive = MutableStateFlow(false)
    val isAudioActive: StateFlow<Boolean> = _isAudioActive.asStateFlow()

    fun start() {
        if (isEngineRunning) return
        isEngineRunning = true

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        val trackBufferSize = maxOf(minBufferSize, BUFFER_SIZE * 4)

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(trackBufferSize)
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()

        audioTrack?.play()
        _isAudioActive.value = true

        synthJob = scope.launch {
            audioLoop()
        }
    }

    fun stop() {
        isEngineRunning = false
        synthJob?.cancel()
        audioTrack?.stop()
        audioTrack?.release()
        audioTrack = null
        _isAudioActive.value = false
    }

    fun noteOn(midiNote: Int, velocity: Float = 0.9f) {
        synchronized(activeNotes) {
            val freq = midiNoteToFreq(midiNote)
            activeNotes.removeAll { it.midiNote == midiNote }
            activeNotes.add(NoteState(midiNote, freq, velocity))
        }
    }

    fun noteOff(midiNote: Int) {
        synchronized(activeNotes) {
            activeNotes.find { it.midiNote == midiNote }?.let {
                it.isReleasing = true
            }
        }
    }

    fun stopAllNotes() {
        synchronized(activeNotes) {
            activeNotes.clear()
        }
    }

    fun playDrumPad(padIndex: Int) {
        // Map drum pads to synthetic drum sounds
        when (padIndex) {
            0 -> noteOn(36, 1.0f) // Kick C1
            1 -> noteOn(38, 0.9f) // Snare D1
            2 -> noteOn(42, 0.8f) // Closed Hi-Hat
            3 -> noteOn(46, 0.8f) // Open Hi-Hat
            4 -> noteOn(45, 0.9f) // Low Tom
            5 -> noteOn(48, 0.9f) // Mid Tom
            6 -> noteOn(49, 0.95f) // Crash Cymbal
            7 -> noteOn(51, 0.85f) // Ride Cymbal
        }
        // Auto release drum trigger quickly
        scope.launch {
            kotlinx.coroutines.delay(120)
            when (padIndex) {
                0 -> noteOff(36)
                1 -> noteOff(38)
                2 -> noteOff(42)
                3 -> noteOff(46)
                4 -> noteOff(45)
                5 -> noteOff(48)
                6 -> noteOff(49)
                7 -> noteOff(51)
            }
        }
    }

    private suspend fun audioLoop() {
        val pcmBuffer = ShortArray(BUFFER_SIZE)
        val vizChunk = FloatArray(256)
        var vizIdx = 0

        val attackSamples = (attackMs * SAMPLE_RATE / 1000f).coerceAtLeast(1f)
        val decaySamples = (decayMs * SAMPLE_RATE / 1000f).coerceAtLeast(1f)
        val releaseSamplesTotal = (releaseMs * SAMPLE_RATE / 1000f).coerceAtLeast(1f)

        while (isEngineRunning) {
            val currentLfoStep = (2.0 * PI * lfoRateHz) / SAMPLE_RATE

            for (i in 0 until BUFFER_SIZE) {
                lfoPhase += currentLfoStep
                if (lfoPhase > 2.0 * PI) lfoPhase -= 2.0 * PI
                val lfoVal = sin(lfoPhase).toFloat() * lfoDepth

                var mixedSample = 0.0f

                synchronized(activeNotes) {
                    val iterator = activeNotes.iterator()
                    while (iterator.hasNext()) {
                        val note = iterator.next()

                        // Calculate Frequency with LFO Pitch Mod
                        val effectiveFreq = if (lfoTarget == LfoTarget.PITCH) {
                            note.frequency * (1.0f + lfoVal * 0.05f)
                        } else {
                            note.frequency
                        }

                        // Calculate Envelope (ADSR)
                        val envAmp: Float
                        if (!note.isReleasing) {
                            note.ageSamples++
                            val age = note.ageSamples.toFloat()
                            envAmp = when {
                                age < attackSamples -> age / attackSamples
                                age < (attackSamples + decaySamples) -> {
                                    1.0f - (1.0f - sustainLevel) * ((age - attackSamples) / decaySamples)
                                }
                                else -> sustainLevel
                            }
                        } else {
                            note.releaseSamples++
                            val rel = note.releaseSamples.toFloat()
                            envAmp = sustainLevel * (1.0f - (rel / releaseSamplesTotal)).coerceIn(0f, 1f)
                            if (rel >= releaseSamplesTotal) {
                                iterator.remove()
                                continue
                            }
                        }

                        // Generate Oscillator Waveform
                        val rawSample = generateWaveform(oscillatorType, effectiveFreq, note.ageSamples)
                        mixedSample += rawSample * envAmp * note.velocity
                    }
                }

                // Apply LFO Amp Mod
                if (lfoTarget == LfoTarget.AMP) {
                    mixedSample *= (1.0f + lfoVal * 0.5f).coerceIn(0f, 1f)
                }

                // Apply Low-Pass Filter
                val effectiveCutoff = if (lfoTarget == LfoTarget.CUTOFF) {
                    (filterCutoffHz * (1.0f + lfoVal)).coerceIn(20f, 18000f)
                } else {
                    filterCutoffHz
                }
                val alpha = (2.0 * PI * effectiveCutoff / SAMPLE_RATE).toFloat().coerceIn(0.01f, 0.99f)
                filterLowPassState += alpha * (mixedSample - filterLowPassState)
                var filteredSample = filterLowPassState

                // Apply Overdrive Distortion
                if (distortionLevel > 0.05f) {
                    val drive = 1.0f + distortionLevel * 5.0f
                    filteredSample = (filteredSample * drive).coerceIn(-0.95f, 0.95f)
                }

                // Apply Delay Effect
                if (delayLevel > 0.05f) {
                    val readPos = (delayWritePos - (delayBufferLength * 0.75).toInt() + delayBufferLength) % delayBufferLength
                    val delayedSample = delayBuffer[readPos]
                    delayBuffer[delayWritePos] = filteredSample + delayedSample * 0.4f
                    filteredSample += delayedSample * delayLevel
                    delayWritePos = (delayWritePos + 1) % delayBufferLength
                }

                // Master Gain
                val finalSample = (filteredSample * masterVolume).coerceIn(-0.98f, 0.98f)
                pcmBuffer[i] = (finalSample * 32767f).toInt().toShort()

                // Save to visualizer buffer
                if (i % 4 == 0 && vizIdx < 256) {
                    vizChunk[vizIdx++] = finalSample
                }
            }

            // Update visualizer frame
            _visualizerBuffer.value = vizChunk.copyOf()
            vizIdx = 0

            // Write PCM to AudioTrack
            audioTrack?.write(pcmBuffer, 0, BUFFER_SIZE)
        }
    }

    private fun generateWaveform(type: OscillatorType, freq: Float, sampleCount: Long): Float {
        val t = (sampleCount.toDouble() / SAMPLE_RATE)
        val phase = (t * freq) % 1.0

        return when (type) {
            OscillatorType.SINE -> sin(2.0 * PI * phase).toFloat()
            OscillatorType.SAWTOOTH -> (2.0 * phase - 1.0).toFloat()
            OscillatorType.SQUARE -> if (phase < 0.5) 1.0f else -1.0f
            OscillatorType.TRIANGLE -> (2.0 * abs(2.0 * phase - 1.0) - 1.0).toFloat()
            OscillatorType.NOISE -> (Random.nextFloat() * 2.0f - 1.0f)
            OscillatorType.WAVETABLE -> {
                // Dual Harmonic Wavetable
                (sin(2.0 * PI * phase) + 0.5 * sin(4.0 * PI * phase) + 0.25 * sin(6.0 * PI * phase)).toFloat() / 1.75f
            }
            OscillatorType.FM -> {
                // Frequency Modulation Synthesis
                val modulator = sin(2.0 * PI * phase * 2.0) * 1.5
                sin(2.0 * PI * phase + modulator).toFloat()
            }
            OscillatorType.GRANULAR -> {
                // Granular Burst Waveform
                val grainVal = sin(2.0 * PI * phase)
                val pulse = sin(10.0 * PI * phase)
                (grainVal * pulse).toFloat()
            }
        }
    }

    private fun midiNoteToFreq(note: Int): Float {
        return 440.0f * Math.pow(2.0, (note - 69) / 12.0).toFloat()
    }
}
