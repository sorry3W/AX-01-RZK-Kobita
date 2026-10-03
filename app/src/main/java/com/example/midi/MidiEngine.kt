package com.example.midi

import com.example.data.db.MidiMappingEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MIDI Engine for AX-100 AI Z-File DAW Integration.
 * Handles DAW CC Mapping, MIDI Signal Generation, Transport Control, and Signal Monitoring.
 */
class MidiEngine {

    data class MidiLogEvent(
        val timestamp: String,
        val channel: Int,
        val type: String, // "CC", "NoteOn", "NoteOff", "Clock", "Transport"
        val parameter: String,
        val value: Int,
        val hexPayload: String
    )

    enum class DawProfile(val displayName: String, val description: String) {
        ABLETON("Ableton Live", "Macro 1-8, Filter Cutoff (CC74), Res (CC71), Volume (CC7)"),
        LOGIC_PRO("Logic Pro", "Smart Controls, Synth VCF (CC74), Env Attack (CC73)"),
        FL_STUDIO("FL Studio", "Peak Controller, Cutoff (CC74), LFO Rate (CC12)"),
        CUBASE("Cubase VST", "Quick Controls 1-8, Synth Params CC80-CC88"),
        PRO_TOOLS("Pro Tools", "MIDI Automation CC20-CC28")
    }

    // State Flows
    private val _currentBpm = MutableStateFlow(120)
    val currentBpm: StateFlow<Int> = _currentBpm.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isLooping = MutableStateFlow(true)
    val isLooping: StateFlow<Boolean> = _isLooping.asStateFlow()

    private val _selectedDawProfile = MutableStateFlow(DawProfile.ABLETON)
    val selectedDawProfile: StateFlow<DawProfile> = _selectedDawProfile.asStateFlow()

    private val _midiLogs = MutableStateFlow<List<MidiLogEvent>>(emptyList())
    val midiLogs: StateFlow<List<MidiLogEvent>> = _midiLogs.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    init {
        // Initial log
        addLog("SYS", "DAW Sync Initialized", 0, "B0 00 00")
    }

    fun setBpm(bpm: Int) {
        _currentBpm.value = bpm.coerceIn(40, 240)
        addLog("Clock", "BPM Updated to ${_currentBpm.value}", _currentBpm.value, "F8")
    }

    fun togglePlay() {
        _isPlaying.value = !_isPlaying.value
        val action = if (_isPlaying.value) "START PLAYBACK" else "PAUSE"
        addLog("Transport", action, if (_isPlaying.value) 1 else 0, if (_isPlaying.value) "FA" else "FC")
    }

    fun toggleRecord() {
        _isRecording.value = !_isRecording.value
        addLog("Transport", if (_isRecording.value) "ARM RECORDING" else "RECORD OFF", if (_isRecording.value) 127 else 0, "C0 01")
    }

    fun toggleLoop() {
        _isLooping.value = !_isLooping.value
        addLog("Transport", if (_isLooping.value) "LOOP ENABLED" else "LOOP DISABLED", 0, "B0 75")
    }

    fun setDawProfile(profile: DawProfile) {
        _selectedDawProfile.value = profile
        addLog("DAW Profile", "Switched to ${profile.displayName}", 0, "B0 00 ${profile.ordinal}")
    }

    fun sendMidiCc(ccNumber: Int, value: Int, parameterName: String) {
        val hexValue = value.toString(16).uppercase().padStart(2, '0')
        val hexCc = ccNumber.toString(16).uppercase().padStart(2, '0')
        addLog("CC#$ccNumber", parameterName, value, "B0 $hexCc $hexValue")
    }

    fun sendNoteOn(noteNumber: Int, velocity: Int) {
        val hexNote = noteNumber.toString(16).uppercase().padStart(2, '0')
        val hexVel = velocity.toString(16).uppercase().padStart(2, '0')
        addLog("NoteOn", "MIDI Note $noteNumber", velocity, "90 $hexNote $hexVel")
    }

    fun clearLogs() {
        _midiLogs.value = emptyList()
    }

    private fun addLog(type: String, param: String, value: Int, hexPayload: String) {
        val event = MidiLogEvent(
            timestamp = dateFormat.format(Date()),
            channel = 1,
            type = type,
            parameter = param,
            value = value,
            hexPayload = hexPayload
        )
        val updated = listOf(event) + _midiLogs.value
        _midiLogs.value = updated.take(50) // Keep latest 50 events
    }

    fun generateDefaultMappings(dawName: String): List<MidiMappingEntity> {
        return listOf(
            MidiMappingEntity(ccNumber = 74, parameterName = "Filter Cutoff", dawProfile = dawName, sourceAttribute = "File Entropy", currentCcValue = 85),
            MidiMappingEntity(ccNumber = 71, parameterName = "Filter Resonance", dawProfile = dawName, sourceAttribute = "Byte Complexity", currentCcValue = 40),
            MidiMappingEntity(ccNumber = 73, parameterName = "Envelope Attack", dawProfile = dawName, sourceAttribute = "Word Count", currentCcValue = 25),
            MidiMappingEntity(ccNumber = 72, parameterName = "Envelope Release", dawProfile = dawName, sourceAttribute = "File Size", currentCcValue = 60),
            MidiMappingEntity(ccNumber = 1, parameterName = "Modulation / LFO", dawProfile = dawName, sourceAttribute = "Text Sentiment", currentCcValue = 50),
            MidiMappingEntity(ccNumber = 91, parameterName = "Reverb Send", dawProfile = dawName, sourceAttribute = "Color Palette", currentCcValue = 35),
            MidiMappingEntity(ccNumber = 93, parameterName = "Delay Send", dawProfile = dawName, sourceAttribute = "File Hash", currentCcValue = 20),
            MidiMappingEntity(ccNumber = 7, parameterName = "Master Track Vol", dawProfile = dawName, sourceAttribute = "Master Gain", currentCcValue = 100)
        )
    }
}
