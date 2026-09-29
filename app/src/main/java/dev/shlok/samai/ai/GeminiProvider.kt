package dev.shlok.samai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class GeminiProvider(private var apiKey: String) : AIProvider {
    override val name = "gemini"
    override val displayName = "Google Gemini"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    override fun isConfigured() = apiKey.isNotBlank()

    fun updateKey(key: String) { apiKey = key }

    override suspend fun chat(messages: List<AIMessage>): AIResponse = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext AIResponse("", true, "Gemini API key not configured.")
        try {
            // Convert messages to Gemini format (skip system messages, merge into user)
            val contents = messages
                .filter { it.role != "system" }
                .map { msg ->
                    mapOf(
                        "role" to if (msg.role == "user") "user" else "model",
                        "parts" to listOf(mapOf("text" to msg.content))
                    )
                }

            val body = mapOf("contents" to contents)
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .header("Content-Type", "application/json")
                .post(gson.toJson(body).toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext AIResponse("", true, "Gemini error ${response.code}")
            }
            val parsed = gson.fromJson(responseBody, GeminiResponse::class.java)
            val text = parsed.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No response."
            AIResponse(text)
        } catch (e: Exception) {
            AIResponse("", true, "Gemini connection failed: ${e.message}")
        }
    }

    override suspend fun testConnection(): Boolean {
        return chat(listOf(AIMessage("user", "Hi"))).let { !it.isError }
    }

    data class GeminiResponse(val candidates: List<Candidate>?)
    data class Candidate(val content: Content?)
    data class Content(val parts: List<Part>?)
    data class Part(val text: String?)
}
