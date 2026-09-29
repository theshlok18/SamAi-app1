package dev.shlok.samai.ai

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import com.google.gson.Gson
import java.util.concurrent.TimeUnit

class OpenAIProvider(private var apiKey: String) : AIProvider {
    override val name = "openai"
    override val displayName = "OpenAI GPT"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    override fun isConfigured() = apiKey.isNotBlank()

    fun updateKey(key: String) { apiKey = key }

    override suspend fun chat(messages: List<AIMessage>): AIResponse = withContext(Dispatchers.IO) {
        if (!isConfigured()) return@withContext AIResponse("", true, "OpenAI API key not configured.")
        try {
            val body = mapOf(
                "model" to "gpt-3.5-turbo",
                "messages" to messages.map { mapOf("role" to it.role, "content" to it.content) },
                "max_tokens" to 1024
            )
            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer $apiKey")
                .header("Content-Type", "application/json")
                .post(gson.toJson(body).toRequestBody("application/json".toMediaType()))
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext AIResponse("", true, "OpenAI error ${response.code}: $responseBody")
            }
            val parsed = gson.fromJson(responseBody, OpenAIChatResponse::class.java)
            val text = parsed.choices?.firstOrNull()?.message?.content ?: "No response."
            AIResponse(text)
        } catch (e: Exception) {
            AIResponse("", true, "OpenAI connection failed: ${e.message}")
        }
    }

    override suspend fun testConnection(): Boolean {
        return chat(listOf(AIMessage("user", "Hi"))).let { !it.isError }
    }

    data class OpenAIChatResponse(val choices: List<Choice>?)
    data class Choice(val message: MessageContent?)
    data class MessageContent(val content: String?)
}
