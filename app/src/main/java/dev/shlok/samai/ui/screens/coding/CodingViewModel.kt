package dev.shlok.samai.ui.screens.coding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shlok.samai.ai.AIMessage
import dev.shlok.samai.ai.ModelRouter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CodeProject(
    val id: String = System.currentTimeMillis().toString(),
    val name: String,
    val language: String,
    val code: String,
    val output: String = "",
    val isHtml: Boolean = language.lowercase() == "html"
)

data class CodingUiState(
    val projects: List<CodeProject> = emptyList(),
    val activeProject: CodeProject? = null,
    val isGenerating: Boolean = false,
    val prompt: String = "",
    val showPreview: Boolean = false,
    val output: String = "",
    val error: String = ""
)

@HiltViewModel
class CodingViewModel @Inject constructor(
    private val modelRouter: ModelRouter
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodingUiState())
    val uiState: StateFlow<CodingUiState> = _uiState.asStateFlow()

    fun updatePrompt(p: String) = _uiState.update { it.copy(prompt = p) }

    fun updateCode(code: String) {
        _uiState.update { state ->
            state.copy(activeProject = state.activeProject?.copy(code = code))
        }
    }

    fun generateCode() {
        val prompt = _uiState.value.prompt.trim()
        if (prompt.isBlank()) return
        _uiState.update { it.copy(isGenerating = true, error = "") }

        viewModelScope.launch {
            val provider = modelRouter.getActiveProvider()
            if (provider == null) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = "No AI model configured. Add an API key in Settings."
                    )
                }
                return@launch
            }

            val systemMsg = """
                You are a code generation assistant. Generate clean, complete, working code.
                Return ONLY the code without explanation unless asked.
                For HTML/CSS/JS projects, return a single complete HTML file with embedded CSS and JS.
                Use code blocks with language tags: ```html, ```python, ```kotlin etc.
            """.trimIndent()

            val response = provider.chat(
                listOf(
                    AIMessage("system", systemMsg),
                    AIMessage("user", "Generate code for: $prompt")
                )
            )

            if (response.isError) {
                _uiState.update { it.copy(isGenerating = false, error = response.errorMessage) }
                return@launch
            }

            val (language, code) = extractCode(response.text)
            val project = CodeProject(
                name = prompt.take(40),
                language = language,
                code = code
            )

            _uiState.update { state ->
                state.copy(
                    isGenerating = false,
                    activeProject = project,
                    projects = listOf(project) + state.projects.take(9),
                    prompt = ""
                )
            }
        }
    }

    fun aiExplain() {
        val code = _uiState.value.activeProject?.code ?: return
        _uiState.update { it.copy(isGenerating = true) }
        viewModelScope.launch {
            val provider = modelRouter.getActiveProvider() ?: run {
                _uiState.update { it.copy(isGenerating = false, error = "No AI model configured.") }
                return@launch
            }
            val response = provider.chat(listOf(
                AIMessage("system", "Explain this code clearly and concisely."),
                AIMessage("user", code)
            ))
            _uiState.update {
                it.copy(
                    isGenerating = false,
                    output = if (response.isError) "Error: ${response.errorMessage}" else response.text
                )
            }
        }
    }

    fun aiFix() {
        val code = _uiState.value.activeProject?.code ?: return
        _uiState.update { it.copy(isGenerating = true) }
        viewModelScope.launch {
            val provider = modelRouter.getActiveProvider() ?: run {
                _uiState.update { it.copy(isGenerating = false, error = "No AI model configured.") }
                return@launch
            }
            val response = provider.chat(listOf(
                AIMessage("system", "Fix any bugs in this code. Return only the fixed code."),
                AIMessage("user", code)
            ))
            if (!response.isError) {
                val (_, fixedCode) = extractCode(response.text)
                _uiState.update { state ->
                    state.copy(
                        isGenerating = false,
                        activeProject = state.activeProject?.copy(code = fixedCode)
                    )
                }
            } else {
                _uiState.update { it.copy(isGenerating = false, error = response.errorMessage) }
            }
        }
    }

    fun togglePreview() {
        _uiState.update { it.copy(showPreview = !it.showPreview) }
    }

    fun selectProject(project: CodeProject) {
        _uiState.update { it.copy(activeProject = project) }
    }

    private fun extractCode(text: String): Pair<String, String> {
        val codeBlockRegex = Regex("```(\\w+)?\\n([\\s\\S]+?)```")
        val match = codeBlockRegex.find(text)
        return if (match != null) {
            val lang = match.groupValues[1].ifBlank { "text" }
            val code = match.groupValues[2].trim()
            Pair(lang, code)
        } else {
            Pair("text", text.trim())
        }
    }
}
