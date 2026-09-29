package dev.shlok.samai.profile

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sam_preferences")

data class UserProfile(
    val userName: String = "Shlok",
    val assistantName: String = "SAM",
    val wakeWord: String = "Hey Sam",
    val responseStyle: String = "balanced",
    val voiceEnabled: Boolean = true,
    val onboardingComplete: Boolean = false
)

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val USER_NAME = stringPreferencesKey("user_name")
        val ASSISTANT_NAME = stringPreferencesKey("assistant_name")
        val WAKE_WORD = stringPreferencesKey("wake_word")
        val RESPONSE_STYLE = stringPreferencesKey("response_style")
        val VOICE_ENABLED = booleanPreferencesKey("voice_enabled")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val SELECTED_AI_PROVIDER = stringPreferencesKey("selected_ai_provider")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val GEMINI_API_KEY = stringPreferencesKey("gemini_api_key")
        val ANTHROPIC_API_KEY = stringPreferencesKey("anthropic_api_key")
        val CUSTOM_API_URL = stringPreferencesKey("custom_api_url")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_api_key")
    }

    val userProfileFlow: Flow<UserProfile> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            UserProfile(
                userName = prefs[USER_NAME] ?: "Shlok",
                assistantName = prefs[ASSISTANT_NAME] ?: "SAM",
                wakeWord = prefs[WAKE_WORD] ?: "Hey Sam",
                responseStyle = prefs[RESPONSE_STYLE] ?: "balanced",
                voiceEnabled = prefs[VOICE_ENABLED] ?: true,
                onboardingComplete = prefs[ONBOARDING_COMPLETE] ?: false
            )
        }

    suspend fun updateUserName(name: String) {
        context.dataStore.edit { it[USER_NAME] = name }
    }

    suspend fun updateAssistantName(name: String) {
        context.dataStore.edit { it[ASSISTANT_NAME] = name }
    }

    suspend fun updateWakeWord(word: String) {
        context.dataStore.edit { it[WAKE_WORD] = word }
    }

    suspend fun updateResponseStyle(style: String) {
        context.dataStore.edit { it[RESPONSE_STYLE] = style }
    }

    suspend fun updateVoiceEnabled(enabled: Boolean) {
        context.dataStore.edit { it[VOICE_ENABLED] = enabled }
    }

    suspend fun setOnboardingComplete() {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = true }
    }

    suspend fun saveApiKey(provider: String, key: String) {
        context.dataStore.edit { prefs ->
            when (provider) {
                "openai" -> prefs[OPENAI_API_KEY] = key
                "gemini" -> prefs[GEMINI_API_KEY] = key
                "anthropic" -> prefs[ANTHROPIC_API_KEY] = key
                "custom" -> prefs[CUSTOM_API_KEY] = key
            }
        }
    }

    suspend fun saveCustomApiUrl(url: String) {
        context.dataStore.edit { it[CUSTOM_API_URL] = url }
    }

    suspend fun setSelectedProvider(provider: String) {
        context.dataStore.edit { it[SELECTED_AI_PROVIDER] = provider }
    }

    val aiSettingsFlow: Flow<AISettings> = context.dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            AISettings(
                selectedProvider = prefs[SELECTED_AI_PROVIDER] ?: "none",
                openAiKey = prefs[OPENAI_API_KEY] ?: "",
                geminiKey = prefs[GEMINI_API_KEY] ?: "",
                anthropicKey = prefs[ANTHROPIC_API_KEY] ?: "",
                customApiUrl = prefs[CUSTOM_API_URL] ?: "",
                customApiKey = prefs[CUSTOM_API_KEY] ?: ""
            )
        }
}

data class AISettings(
    val selectedProvider: String = "none",
    val openAiKey: String = "",
    val geminiKey: String = "",
    val anthropicKey: String = "",
    val customApiUrl: String = "",
    val customApiKey: String = ""
)
