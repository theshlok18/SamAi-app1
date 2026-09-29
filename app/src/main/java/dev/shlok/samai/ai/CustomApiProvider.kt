package dev.shlok.samai.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class CustomApiProvider(private val baseUrl: String, private val apiKey: String) : AIProvider {
    override val name = "custom"
    override val displayName = "Custom API"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    override fun isConfigured() = baseUrl.isNotBlank()

    override suspend fun chat(messages: List<AIMessage>): AIResponse = withContext(Dispatchers.IO) {
        try {
            val body = mapOf(
                "messages" to messages.map { mapOf("role" to it.role, "content" to it.content) }
            )
            val builder = Request.Builder()
                .url("$baseUrl/chat/completions")
                .header("Content-Type", "application/json")
                .post(gson.toJson(body).toRequestBody("application/json".toMediaType()))
            if (apiKey.isNotBlank()) builder.header("Authorization", "Bearer $apiKey")
            val response = client.newCall(builder.build()).execute()
            val text = response.body?.string() ?: "No response."
            if (!response.isSuccessful) return@withContext AIResponse("", true, "Custom API error ${response.code}")
            AIResponse(text)
        } catch (e: Exception) {
            AIResponse("", true, "Custom API failed: ${e.message}")
        }
    }

    override suspend fun testConnection(): Boolean =
        chat(listOf(AIMessage("user", "Hi"))).let { !it.isError }
}
