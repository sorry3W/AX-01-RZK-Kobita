package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "z_files")
data class ZFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fileName: String,
    val fileType: String, // PDF, DOCX, TXT, ZIP, PNG, MP3, MP4, CSV, JSON, CODE
    val fileSizeFormatted: String,
    val entropyScore: Float, // 0.0 to 1.0 file density/complexity
    val wordCount: Int,
    val detectedMusicalKey: String, // e.g. "C Major", "A Minor", "F# Dorian"
    val suggestedTempoBpm: Int, // e.g. 120
    val synthOscillator: String,
    val cutoffFrequency: Float,
    val aiSummary: String,
    val status: String = "SONIFIED", // UNPROCESSED, ANALYZING, SONIFIED
    val timestamp: Long = System.currentTimeMillis()
)
