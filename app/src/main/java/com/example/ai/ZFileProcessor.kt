package com.example.ai

import com.example.audio.AudioSynthEngine
import com.example.data.db.PatchPresetEntity
import com.example.data.db.ZFileEntity
import kotlin.math.ln

/**
 * File Sonification & Semantic Feature Extraction Engine for AX-100 Z-File Device.
 * Supports both online Gemini AI sonification and 100% offline local AI sonification logic.
 */
class ZFileProcessor {

    private val geminiService = GeminiZFileService()

    data class AnalysisResult(
        val zFileEntity: ZFileEntity,
        val patchPreset: PatchPresetEntity
    )

    suspend fun processFile(
        fileName: String,
        mimeType: String,
        contentBytes: ByteArray
    ): AnalysisResult {
        val fileSizeFormatted = formatFileSize(contentBytes.size)
        val entropy = calculateShannonEntropy(contentBytes)
        val textSnippet = String(contentBytes.take(1024).toByteArray(), Charsets.UTF_8)
            .filter { it.isLetterOrDigit() || it.isWhitespace() }
        val wordCount = textSnippet.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size

        val extension = fileName.substringAfterLast('.', "TXT").uppercase()

        // Attempt Gemini AI Analysis
        val geminiRawResponse = geminiService.generateZFileSonification(
            fileName = fileName,
            fileType = extension,
            fileContentSnippet = textSnippet.take(500),
            entropy = entropy
        )

        val isAiSuccess = geminiRawResponse != "OFFLINE_FALLBACK" && geminiRawResponse.isNotEmpty()

        // Derive Parameters (using local intelligent rule engine seeded by entropy + AI response)
        val oscillator = selectOscillator(extension, entropy, geminiRawResponse)
        val cutoff = deriveCutoff(entropy, contentBytes.size)
        val key = deriveMusicalKey(contentBytes)
        val tempo = deriveTempo(entropy, wordCount)
        val summary = if (isAiSuccess) {
            "AI Sonified ($extension): ${geminiRawResponse.take(160)}"
        } else {
            "Local Offline AI Sonified ($extension): Sonified from $fileSizeFormatted data with entropy $entropy."
        }

        val zEntity = ZFileEntity(
            fileName = fileName,
            fileType = extension,
            fileSizeFormatted = fileSizeFormatted,
            entropyScore = entropy,
            wordCount = wordCount,
            detectedMusicalKey = key,
            suggestedTempoBpm = tempo,
            synthOscillator = oscillator.name,
            cutoffFrequency = cutoff,
            aiSummary = summary,
            status = "SONIFIED"
        )

        val patch = PatchPresetEntity(
            name = "Z-Patch: ${fileName.take(18)}",
            category = "File Sonification",
            oscillatorType = oscillator.name,
            attackMs = (50f + entropy * 200f).coerceIn(10f, 800f),
            decayMs = (150f + (1f - entropy) * 400f).coerceIn(50f, 1000f),
            sustainLevel = (0.3f + entropy * 0.5f).coerceIn(0.2f, 0.95f),
            releaseMs = (200f + entropy * 600f).coerceIn(100f, 2000f),
            filterCutoffHz = cutoff,
            filterResonance = (0.2f + entropy * 0.6f).coerceIn(0.1f, 0.9f),
            lfoRateHz = (1f + entropy * 8f).coerceIn(0.5f, 12f),
            lfoDepth = (0.1f + entropy * 0.5f).coerceIn(0.05f, 0.8f),
            reverbLevel = (0.2f + (contentBytes.size % 100) / 200f).coerceIn(0.1f, 0.7f),
            delayLevel = (0.1f + entropy * 0.4f).coerceIn(0.05f, 0.6f),
            distortionLevel = if (entropy > 0.8f) (entropy - 0.7f) * 2f else 0.0f,
            isZFileGenerated = true,
            sourceFileName = fileName
        )

        return AnalysisResult(zEntity, patch)
    }

    private fun calculateShannonEntropy(data: ByteArray): Float {
        if (data.isEmpty()) return 0f
        val counts = IntArray(256)
        for (b in data) {
            counts[b.toInt() and 0xFF]++
        }
        var entropy = 0.0
        val total = data.size.toDouble()
        for (c in counts) {
            if (c > 0) {
                val p = c / total
                entropy -= p * (ln(p) / ln(2.0))
            }
        }
        return (entropy / 8.0).toFloat().coerceIn(0f, 1f)
    }

    private fun selectOscillator(
        ext: String,
        entropy: Float,
        aiText: String
    ): AudioSynthEngine.OscillatorType {
        if (aiText.contains("FM", ignoreCase = true)) return AudioSynthEngine.OscillatorType.FM
        if (aiText.contains("WAVETABLE", ignoreCase = true)) return AudioSynthEngine.OscillatorType.WAVETABLE
        if (aiText.contains("GRANULAR", ignoreCase = true)) return AudioSynthEngine.OscillatorType.GRANULAR

        return when {
            ext in listOf("PDF", "DOCX", "TXT") -> {
                if (entropy > 0.6f) AudioSynthEngine.OscillatorType.WAVETABLE else AudioSynthEngine.OscillatorType.SAWTOOTH
            }
            ext in listOf("ZIP", "EXE", "BIN") -> AudioSynthEngine.OscillatorType.GRANULAR
            ext in listOf("PNG", "JPG", "WEBP") -> AudioSynthEngine.OscillatorType.FM
            ext in listOf("JSON", "CSV", "XML", "KT", "JAVA", "PY") -> AudioSynthEngine.OscillatorType.SQUARE
            ext in listOf("MP3", "WAV", "OGG") -> AudioSynthEngine.OscillatorType.TRIANGLE
            else -> if (entropy > 0.75f) AudioSynthEngine.OscillatorType.NOISE else AudioSynthEngine.OscillatorType.SINE
        }
    }

    private fun deriveCutoff(entropy: Float, size: Int): Float {
        val base = 1200f + entropy * 6000f
        val sizeMod = (size % 2000)
        return (base + sizeMod).coerceIn(400f, 12000f)
    }

    private fun deriveMusicalKey(data: ByteArray): String {
        val keys = listOf(
            "C Major", "G Major", "D Major", "A Minor", "E Minor",
            "F# Cyberpunk", "D Phrygian", "C# Neon Minor", "Bb Ambient"
        )
        val hash = data.sumOf { it.toInt() and 0xFF }
        return keys[hash % keys.size]
    }

    private fun deriveTempo(entropy: Float, wordCount: Int): Int {
        val baseBpm = 85 + (entropy * 50).toInt() + (wordCount % 20)
        return baseBpm.coerceIn(70, 160)
    }

    private fun formatFileSize(bytes: Int): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> String.format("%.2f MB", bytes / (1024f * 1024f))
        }
    }
}
