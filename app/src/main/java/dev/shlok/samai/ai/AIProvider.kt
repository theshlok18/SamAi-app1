package dev.shlok.samai.ai

data class AIMessage(
    val role: String, // "system", "user", "assistant"
    val content: String
)

data class AIResponse(
    val text: String,
    val isError: Boolean = false,
    val errorMessage: String = ""
)

interface AIProvider {
    val name: String
    val displayName: String
    suspend fun chat(messages: List<AIMessage>): AIResponse
    suspend fun testConnection(): Boolean
    fun isConfigured(): Boolean
}
