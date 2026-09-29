package dev.shlok.samai.ui.screens.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.shlok.samai.ai.AIOrchestrator
import dev.shlok.samai.androidcontrol.SamAccessibilityService
import dev.shlok.samai.profile.UserPreferences
import dev.shlok.samai.tasks.Task
import dev.shlok.samai.tasks.TaskExecutor
import dev.shlok.samai.voice.ListeningState
import dev.shlok.samai.voice.SamSpeechRecognizer
import dev.shlok.samai.voice.SamTextToSpeech
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "Shlok",
    val isOnline: Boolean = true,
    val isListening: Boolean = false,
    val voiceLevel: Float = 0f,
    val samResponse: String = "",
    val isThinking: Boolean = false,
    val accessibilityConnected: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences,
    private val orchestrator: AIOrchestrator,
    private val speechRecognizer: SamSpeechRecognizer,
    private val textToSpeech: SamTextToSpeech,
    private val taskExecutor: TaskExecutor
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val activeTask: StateFlow<Task?> = taskExecutor.activeTask

    init {
        viewModelScope.launch {
            userPreferences.userProfileFlow.collect { profile ->
                _uiState.update { it.copy(userName = profile.userName) }
            }
        }
        viewModelScope.launch {
            speechRecognizer.listeningState.collect { state ->
                _uiState.update {
                    it.copy(
                        isListening = state == ListeningState.LISTENING,
                        isThinking = state == ListeningState.PROCESSING
                    )
                }
            }
        }
        viewModelScope.launch {
            speechRecognizer.rmsLevel.collect { level ->
                _uiState.update { it.copy(voiceLevel = level) }
            }
        }
        viewModelScope.launch {
            SamAccessibilityService.instance.collect { svc ->
                _uiState.update { it.copy(accessibilityConnected = svc != null) }
            }
        }
        checkConnectivity()
    }

    private fun checkConnectivity() {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = cm.activeNetwork?.let { cm.getNetworkCapabilities(it) }
        val isOnline = nc?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        _uiState.update { it.copy(isOnline = isOnline) }
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            speechRecognizer.stopListening()
        } else {
            startListening()
        }
    }

    private fun startListening() {
        speechRecognizer.startListening(
            onResult = { text -> handleVoiceInput(text) },
            onError = { error ->
                _uiState.update { it.copy(samResponse = error, isListening = false, isThinking = false) }
            }
        )
    }

    fun handleVoiceInput(text: String) {
        _uiState.update { it.copy(isThinking = true, samResponse = "Processing...") }
        viewModelScope.launch {
            try {
                val response = orchestrator.process(text)
                val responseText = response.text
                _uiState.update { it.copy(samResponse = responseText, isThinking = false) }
                textToSpeech.speak(responseText)

                response.taskCreated?.let { task ->
                    taskExecutor.execute(task) { updatedTask ->
                        // Task updated - could update UI
                    }
                }
            } catch (e: Exception) {
                val err = "Something went wrong: ${e.message}"
                _uiState.update { it.copy(samResponse = err, isThinking = false) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.stopListening()
    }
}
