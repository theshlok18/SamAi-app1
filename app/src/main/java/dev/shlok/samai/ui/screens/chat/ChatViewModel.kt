package dev.shlok.samai.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shlok.samai.ai.AIOrchestrator
import dev.shlok.samai.memory.ChatMessageEntity
import dev.shlok.samai.memory.ChatMessageDao
import dev.shlok.samai.tasks.Task
import dev.shlok.samai.tasks.TaskExecutor
import dev.shlok.samai.voice.SamSpeechRecognizer
import dev.shlok.samai.voice.SamTextToSpeech
import dev.shlok.samai.voice.ListeningState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val role: String, // "user" or "assistant"
    val content: String,
    val isThinking: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isThinking: Boolean = false,
    val isListening: Boolean = false,
    val inputText: String = ""
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val orchestrator: AIOrchestrator,
    private val chatMessageDao: ChatMessageDao,
    private val speechRecognizer: SamSpeechRecognizer,
    private val textToSpeech: SamTextToSpeech,
    private val taskExecutor: TaskExecutor
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    val activeTask: StateFlow<Task?> = taskExecutor.activeTask

    init {
        loadHistory()
        viewModelScope.launch {
            speechRecognizer.listeningState.collect { state ->
                _uiState.update { it.copy(isListening = state == ListeningState.LISTENING) }
            }
        }
    }

    private fun loadHistory() {
        viewModelScope.launch {
            val history = chatMessageDao.getRecentMessages(30)
            val messages = history.reversed().map {
                ChatMessage(id = it.id, role = it.role, content = it.content)
            }
            _uiState.update { it.copy(messages = messages) }
        }
    }

    fun updateInput(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return
        _uiState.update { it.copy(inputText = "") }
        processInput(text)
    }

    fun sendVoiceMessage(text: String) = processInput(text)

    private fun processInput(text: String) {
        val userMsg = ChatMessage(role = "user", content = text)
        val thinkingMsg = ChatMessage(role = "assistant", content = "", isThinking = true)
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg + thinkingMsg,
                isThinking = true
            )
        }
        viewModelScope.launch {
            chatMessageDao.insertMessage(ChatMessageEntity(role = "user", content = text))
            try {
                val response = orchestrator.process(text)
                val assistantMsg = ChatMessage(role = "assistant", content = response.text)
                _uiState.update { state ->
                    val msgs = state.messages.dropLast(1) + assistantMsg
                    state.copy(messages = msgs, isThinking = false)
                }
                chatMessageDao.insertMessage(ChatMessageEntity(role = "assistant", content = response.text))
                textToSpeech.speak(response.text)
                response.taskCreated?.let { task ->
                    taskExecutor.execute(task) {}
                }
            } catch (e: Exception) {
                val err = ChatMessage(role = "assistant", content = "Something went wrong: ${e.message}")
                _uiState.update { state ->
                    state.copy(messages = state.messages.dropLast(1) + err, isThinking = false)
                }
            }
        }
    }

    fun toggleVoice() {
        if (_uiState.value.isListening) {
            speechRecognizer.stopListening()
        } else {
            speechRecognizer.startListening(
                onResult = { sendVoiceMessage(it) },
                onError = { speechRecognizer.stopListening() }
            )
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatMessageDao.clearAll()
            _uiState.update { it.copy(messages = emptyList()) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechRecognizer.stopListening()
    }
}
