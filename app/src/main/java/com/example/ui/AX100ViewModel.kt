package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.ZFileProcessor
import com.example.audio.AudioSynthEngine
import com.example.data.db.AX100Database
import com.example.data.db.MidiMappingEntity
import com.example.data.db.PatchPresetEntity
import com.example.data.db.ZFileEntity
import com.example.midi.MidiEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AX100ViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AX100Database.getDatabase(application)
    private val dao = db.dao()

    val synthEngine = AudioSynthEngine()
    val midiEngine = MidiEngine()
    private val fileProcessor = ZFileProcessor()

    // Database Flows
    val savedPresets: StateFlow<List<PatchPresetEntity>> = dao.getAllPresets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val midiMappings: StateFlow<List<MidiMappingEntity>> = dao.getAllMidiMappings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val zFiles: StateFlow<List<ZFileEntity>> = dao.getAllZFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Processing States
    private val _isProcessingFile = MutableStateFlow(false)
    val isProcessingFile: StateFlow<Boolean> = _isProcessingFile.asStateFlow()

    private val _lastSonifiedResult = MutableStateFlow<ZFileProcessor.AnalysisResult?>(null)
    val lastSonifiedResult: StateFlow<ZFileProcessor.AnalysisResult?> = _lastSonifiedResult.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Z-File Studio, 1: Synth, 2: MIDI Matrix, 3: Presets & History
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _statusMessage = MutableStateFlow("AX-100 Z-File Core Ready")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    init {
        // Start Audio Engine
        synthEngine.start()

        // Seed initial presets & MIDI mappings if DB is empty
        viewModelScope.launch {
            dao.getAllMidiMappings().collect { list ->
                if (list.isEmpty()) {
                    val defaults = midiEngine.generateDefaultMappings("Ableton Live")
                    dao.insertMidiMappings(defaults)
                }
            }
        }

        viewModelScope.launch {
            dao.getAllPresets().collect { list ->
                if (list.isEmpty()) {
                    seedDefaultPresets()
                }
            }
        }
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun sonifySampleFile(sampleName: String, sampleType: String, content: String) {
        viewModelScope.launch {
            _isProcessingFile.value = true
            _statusMessage.value = "AI Scanning $sampleName..."

            val bytes = content.toByteArray(Charsets.UTF_8)
            val result = fileProcessor.processFile(sampleName, sampleType, bytes)

            // Save to DB
            dao.insertZFile(result.zFileEntity)
            val presetId = dao.insertPreset(result.patchPreset)

            // Load Patch into Audio Synth
            applyPresetToSynth(result.patchPreset)

            _lastSonifiedResult.value = result
            _isProcessingFile.value = false
            _statusMessage.value = "Sonification complete: Loaded Z-Patch into Synthesizer"

            // Log to MIDI engine
            midiEngine.sendMidiCc(74, (result.patchPreset.filterCutoffHz / 100f).toInt().coerceIn(0, 127), "Z-Cutoff Auto Map")
        }
    }

    fun applyPresetToSynth(preset: PatchPresetEntity) {
        synthEngine.oscillatorType = try {
            AudioSynthEngine.OscillatorType.valueOf(preset.oscillatorType)
        } catch (e: Exception) {
            AudioSynthEngine.OscillatorType.SAWTOOTH
        }
        synthEngine.attackMs = preset.attackMs
        synthEngine.decayMs = preset.decayMs
        synthEngine.sustainLevel = preset.sustainLevel
        synthEngine.releaseMs = preset.releaseMs
        synthEngine.filterCutoffHz = preset.filterCutoffHz
        synthEngine.filterResonance = preset.filterResonance
        synthEngine.lfoRateHz = preset.lfoRateHz
        synthEngine.lfoDepth = preset.lfoDepth
        synthEngine.reverbLevel = preset.reverbLevel
        synthEngine.delayLevel = preset.delayLevel
        synthEngine.distortionLevel = preset.distortionLevel

        _statusMessage.value = "Loaded preset: ${preset.name}"
    }

    fun updateSynthOscillator(type: AudioSynthEngine.OscillatorType) {
        synthEngine.oscillatorType = type
    }

    fun updateFilterCutoff(cutoff: Float) {
        synthEngine.filterCutoffHz = cutoff
        midiEngine.sendMidiCc(74, ((cutoff - 200f) / (12000f - 200f) * 127).toInt().coerceIn(0, 127), "Filter Cutoff")
    }

    fun updateFilterResonance(res: Float) {
        synthEngine.filterResonance = res
        midiEngine.sendMidiCc(71, (res * 127).toInt().coerceIn(0, 127), "Resonance")
    }

    fun updateLfoRate(rate: Float) {
        synthEngine.lfoRateHz = rate
        midiEngine.sendMidiCc(12, (rate / 15f * 127).toInt().coerceIn(0, 127), "LFO Rate")
    }

    fun updateLfoDepth(depth: Float) {
        synthEngine.lfoDepth = depth
        midiEngine.sendMidiCc(1, (depth * 127).toInt().coerceIn(0, 127), "LFO Depth / Mod")
    }

    fun updateReverb(level: Float) {
        synthEngine.reverbLevel = level
        midiEngine.sendMidiCc(91, (level * 127).toInt().coerceIn(0, 127), "Reverb Level")
    }

    fun updateDelay(level: Float) {
        synthEngine.delayLevel = level
        midiEngine.sendMidiCc(93, (level * 127).toInt().coerceIn(0, 127), "Delay Level")
    }

    fun updateDistortion(level: Float) {
        synthEngine.distortionLevel = level
    }

    fun triggerNoteOn(midiNote: Int) {
        synthEngine.noteOn(midiNote)
        midiEngine.sendNoteOn(midiNote, 110)
    }

    fun triggerNoteOff(midiNote: Int) {
        synthEngine.noteOff(midiNote)
    }

    fun triggerDrumPad(padIndex: Int) {
        synthEngine.playDrumPad(padIndex)
        midiEngine.sendNoteOn(36 + padIndex, 120)
    }

    fun saveCurrentPreset(name: String) {
        viewModelScope.launch {
            val preset = PatchPresetEntity(
                name = name,
                category = "User Saved",
                oscillatorType = synthEngine.oscillatorType.name,
                attackMs = synthEngine.attackMs,
                decayMs = synthEngine.decayMs,
                sustainLevel = synthEngine.sustainLevel,
                releaseMs = synthEngine.releaseMs,
                filterCutoffHz = synthEngine.filterCutoffHz,
                filterResonance = synthEngine.filterResonance,
                lfoRateHz = synthEngine.lfoRateHz,
                lfoDepth = synthEngine.lfoDepth,
                reverbLevel = synthEngine.reverbLevel,
                delayLevel = synthEngine.delayLevel,
                distortionLevel = synthEngine.distortionLevel
            )
            dao.insertPreset(preset)
            _statusMessage.value = "Preset '$name' saved to memory."
        }
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            dao.deletePresetById(id)
        }
    }

    fun deleteZFile(id: Long) {
        viewModelScope.launch {
            dao.deleteZFileById(id)
        }
    }

    fun updateMidiMappingCc(mapping: MidiMappingEntity, newCc: Int) {
        viewModelScope.launch {
            dao.updateMidiMapping(mapping.copy(ccNumber = newCc))
        }
    }

    private suspend fun seedDefaultPresets() {
        val presets = listOf(
            PatchPresetEntity(
                name = "Cyber Synth Wave",
                category = "Lead",
                oscillatorType = "SAWTOOTH",
                filterCutoffHz = 3500f,
                filterResonance = 0.4f,
                lfoRateHz = 3.5f,
                lfoDepth = 0.3f,
                reverbLevel = 0.35f,
                delayLevel = 0.25f
            ),
            PatchPresetEntity(
                name = "Deep Z-Sub Bass",
                category = "Bass",
                oscillatorType = "SQUARE",
                filterCutoffHz = 800f,
                filterResonance = 0.6f,
                lfoRateHz = 1.0f,
                lfoDepth = 0.2f,
                distortionLevel = 0.3f
            ),
            PatchPresetEntity(
                name = "Ambient PDF Pad",
                category = "Pad",
                oscillatorType = "WAVETABLE",
                attackMs = 400f,
                releaseMs = 1200f,
                filterCutoffHz = 1800f,
                filterResonance = 0.2f,
                reverbLevel = 0.6f,
                delayLevel = 0.4f
            )
        )
        for (p in presets) {
            dao.insertPreset(p)
        }
    }

    override fun onCleared() {
        super.onCleared()
        synthEngine.stop()
    }
}
