package dev.shlok.samai.ai

import dev.shlok.samai.profile.AISettings
import dev.shlok.samai.profile.UserPreferences
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModelRouter @Inject constructor(
    private val userPreferences: UserPreferences
) {
    private val providers = mutableMapOf<String, AIProvider>()

    init {
        providers["openai"] = OpenAIProvider("")
        providers["gemini"] = GeminiProvider("")
    }

    suspend fun updateFromSettings() {
        val settings = userPreferences.aiSettingsFlow.firstOrNull() ?: return
        (providers["openai"] as? OpenAIProvider)?.updateKey(settings.openAiKey)
        (providers["gemini"] as? GeminiProvider)?.updateKey(settings.geminiKey)
    }

    suspend fun getActiveProvider(): AIProvider? {
        val settings = userPreferences.aiSettingsFlow.firstOrNull() ?: return null
        updateFromSettings()
        return providers[settings.selectedProvider]?.takeIf { it.isConfigured() }
            ?: providers.values.firstOrNull { it.isConfigured() }
    }

    fun getProvider(name: String): AIProvider? = providers[name]

    fun getAllProviders(): Map<String, AIProvider> = providers.toMap()

    suspend fun addCustomProvider(url: String, key: String) {
        providers["custom"] = CustomApiProvider(url, key)
        userPreferences.saveCustomApiUrl(url)
        userPreferences.saveApiKey("custom", key)
        userPreferences.setSelectedProvider("custom")
    }
}
