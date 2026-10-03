package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "midi_mappings")
data class MidiMappingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ccNumber: Int,
    val parameterName: String, // e.g. "Filter Cutoff", "LFO Depth", "Reverb Level"
    val dawProfile: String = "Ableton Live", // Ableton, Logic, FL Studio, Pro Tools, Cubase
    val minValue: Int = 0,
    val maxValue: Int = 127,
    val currentCcValue: Int = 64,
    val sourceAttribute: String = "Manual", // e.g., "File Entropy", "Word Count", "Color RGB Red", "Code Lines"
    val isMapped: Boolean = true
)
