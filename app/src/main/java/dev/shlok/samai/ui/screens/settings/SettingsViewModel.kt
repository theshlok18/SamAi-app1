package dev.shlok.samai.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shlok.samai.androidcontrol.SamAccessibilityService
import dev.shlok.samai.profile.AISettings
import dev.shlok.samai.profile.UserPreferences
import dev.shlok.samai.profile.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val userProfile: UserProfile = UserProfile(),
    val aiSettings: AISettings = AISettings(),
    val accessibilityConnected: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferences.userProfileFlow.collect { profile ->
                _uiState.update { it.copy(userProfile = profile) }
            }
        }
        viewModelScope.launch {
            userPreferences.aiSettingsFlow.collect { aiSettings ->
                _uiState.update { it.copy(aiSettings = aiSettings) }
            }
        }
        viewModelScope.launch {
            SamAccessibilityService.instance.collect { svc ->
                _uiState.update { it.copy(accessibilityConnected = svc != null) }
            }
        }
    }

    fun updateUserName(name: String) = viewModelScope.launch { userPreferences.updateUserName(name) }
    fun updateAssistantName(name: String) = viewModelScope.launch { userPreferences.updateAssistantName(name) }
    fun updateWakeWord(word: String) = viewModelScope.launch { userPreferences.updateWakeWord(word) }
    fun updateResponseStyle(style: String) = viewModelScope.launch { userPreferences.updateResponseStyle(style) }
    fun saveOpenAiKey(key: String) = viewModelScope.launch { userPreferences.saveApiKey("openai", key) }
    fun saveGeminiKey(key: String) = viewModelScope.launch { userPreferences.saveApiKey("gemini", key) }
    fun saveAnthropicKey(key: String) = viewModelScope.launch { userPreferences.saveApiKey("anthropic", key) }
    fun setProvider(provider: String) = viewModelScope.launch { userPreferences.setSelectedProvider(provider) }
}
