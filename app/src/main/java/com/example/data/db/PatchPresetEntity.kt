package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patch_presets")
data class PatchPresetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // E.g., "Bass", "Pad", "Lead", "FX", "File Sonification"
    val oscillatorType: String, // SINE, SAWTOOTH, SQUARE, TRIANGLE, NOISE, WAVETABLE, FM, GRANULAR
    val attackMs: Float = 50f,
    val decayMs: Float = 200f,
    val sustainLevel: Float = 0.7f,
    val releaseMs: Float = 300f,
    val filterCutoffHz: Float = 2000f,
    val filterResonance: Float = 0.3f,
    val filterType: String = "LPF", // LPF, HPF, BPF
    val lfoRateHz: Float = 2f,
    val lfoDepth: Float = 0.4f,
    val lfoWaveform: String = "SINE",
    val lfoTarget: String = "CUTOFF", // CUTOFF, PITCH, AMP
    val reverbLevel: Float = 0.3f,
    val delayLevel: Float = 0.2f,
    val distortionLevel: Float = 0.0f,
    val chorusLevel: Float = 0.0f,
    val isZFileGenerated: Boolean = false,
    val sourceFileName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
