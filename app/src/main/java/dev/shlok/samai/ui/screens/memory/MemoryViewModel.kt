package dev.shlok.samai.ui.screens.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shlok.samai.memory.MemoryEntity
import dev.shlok.samai.memory.MemoryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MemoryUiState(
    val memories: List<MemoryEntity> = emptyList(),
    val searchQuery: String = "",
    val showAddDialog: Boolean = false,
    val editingMemory: MemoryEntity? = null,
    val newKey: String = "",
    val newValue: String = ""
)

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()

    init { loadMemories() }

    private fun loadMemories() {
        viewModelScope.launch {
            val mems = memoryRepository.getAllMemories()
            _uiState.update { it.copy(memories = mems) }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        viewModelScope.launch {
            val results = if (query.isBlank()) memoryRepository.getAllMemories()
            else memoryRepository.searchMemories(query)
            _uiState.update { it.copy(memories = results) }
        }
    }

    fun showAddDialog() = _uiState.update {
        it.copy(showAddDialog = true, editingMemory = null, newKey = "", newValue = "")
    }

    fun showEditDialog(memory: MemoryEntity) = _uiState.update {
        it.copy(showAddDialog = true, editingMemory = memory, newKey = memory.key, newValue = memory.value)
    }

    fun dismissDialog() = _uiState.update { it.copy(showAddDialog = false) }

    fun updateNewKey(k: String) = _uiState.update { it.copy(newKey = k) }
    fun updateNewValue(v: String) = _uiState.update { it.copy(newValue = v) }

    fun saveMemory() {
        val key = _uiState.value.newKey.trim()
        val value = _uiState.value.newValue.trim()
        if (key.isBlank() || value.isBlank()) return
        viewModelScope.launch {
            memoryRepository.remember(key, value)
            _uiState.update { it.copy(showAddDialog = false) }
            loadMemories()
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            memoryRepository.deleteMemory(memory)
            loadMemories()
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            memoryRepository.clearAll()
            loadMemories()
        }
    }
}
