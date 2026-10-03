package com.example.ai

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

data class GeminiPart(
    val text: String? = null
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

interface GeminiRestService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiRestService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiRestService::class.java)
    }
}

class GeminiZFileService {

    suspend fun generateZFileSonification(
        fileName: String,
        fileType: String,
        fileContentSnippet: String,
        entropy: Float
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "OFFLINE_FALLBACK"
        }

        val prompt = """
            You are the AX-100 AI Z-File Device Synthesizer AI Engine.
            Analyze this file for real-time sound synthesis and DAW MIDI mapping:
            File Name: $fileName
            File Type: $fileType
            Entropy Score: $entropy
            Snippet Content: $fileContentSnippet

            Generate sound synthesis parameters and musical details:
            Oscillator: SAWTOOTH or SINE or SQUARE or WAVETABLE or FM
            Cutoff: 1000 to 8000 Hz
            Key: e.g. C Major, A Minor, D Phrygian, F# Cyberpunk
            Tempo: 90 to 140 BPM
            Summary: Brief musical sonification concept.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
        )

        try {
            val response = GeminiClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "OFFLINE_FALLBACK"
        } catch (e: Exception) {
            "OFFLINE_FALLBACK"
        }
    }
}
