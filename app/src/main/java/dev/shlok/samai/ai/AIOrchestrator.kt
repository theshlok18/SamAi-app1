package dev.shlok.samai.ai

import dev.shlok.samai.androidcontrol.AndroidController
import dev.shlok.samai.knowledge.WikipediaProvider
import dev.shlok.samai.memory.MemoryRepository
import dev.shlok.samai.profile.DeveloperProfile
import dev.shlok.samai.profile.UserPreferences
import dev.shlok.samai.tasks.Task
import dev.shlok.samai.tasks.TaskPlanner
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

data class OrchestratorResponse(
    val text: String,
    val taskCreated: Task? = null,
    val isError: Boolean = false
)

@Singleton
class AIOrchestrator @Inject constructor(
    private val modelRouter: ModelRouter,
    private val memoryRepository: MemoryRepository,
    private val userPreferences: UserPreferences,
    private val androidController: AndroidController,
    private val taskPlanner: TaskPlanner,
    private val wikiProvider: WikipediaProvider
) {
    private val systemPrompt = """
        You are SAM (Smart Autonomous Machine), a premium AI assistant on Android.
        You are helpful, concise, and professional.
        You assist the user with Android control, coding, knowledge, and conversation.
        When asked to perform an Android action, confirm you will do it.
        When you don't know something, say so honestly.
        Keep responses concise and relevant.
    """.trimIndent()

    suspend fun process(userInput: String): OrchestratorResponse {
        val profile = userPreferences.userProfileFlow.firstOrNull()
        val userName = profile?.userName ?: "Shlok"
        val intent = IntentDetector.detect(userInput)

        return when (intent.type) {
            IntentType.DEVELOPER_INFO ->
                OrchestratorResponse(DeveloperProfile.getDescription())

            IntentType.MEMORY_REQUEST ->
                handleMemoryRequest(userInput, userName)

            IntentType.APP_AUTOMATION, IntentType.TASK_EXECUTION ->
                handleAndroidTask(userInput, intent, userName)

            IntentType.PHONE_COMMAND ->
                handlePhoneCommand(intent, userName)

            IntentType.KNOWLEDGE_QUESTION ->
                handleKnowledgeQuestion(userInput)

            IntentType.CODING_REQUEST ->
                handleWithAI(userInput, "You are a coding assistant. Provide complete, working code with explanations.")

            IntentType.CONVERSATION, IntentType.SETTINGS_REQUEST ->
                handleWithAI(userInput)
        }
    }

    private suspend fun handleMemoryRequest(input: String, userName: String): OrchestratorResponse {
        val lower = input.lowercase()
        if (lower.startsWith("remember") || (lower.contains("my ") && lower.contains(" is "))) {
            val memPair = extractMemoryPair(input)
            if (memPair != null) {
                memoryRepository.remember(memPair.first, memPair.second)
                return OrchestratorResponse("Got it, $userName. I'll remember that.")
            }
        }
        val memContext = memoryRepository.buildMemoryContext()
        return handleWithAI(input, additionalContext = memContext)
    }

    private fun extractMemoryPair(input: String): Pair<String, String>? {
        val patterns = listOf(
            Regex("remember (?:that )?(?:my )?(.+?) is (.+)", RegexOption.IGNORE_CASE),
            Regex("my (.+?) is (.+)", RegexOption.IGNORE_CASE)
        )
        for (p in patterns) {
            val match = p.find(input) ?: continue
            return Pair(match.groupValues[1].trim(), match.groupValues[2].trim())
        }
        return null
    }

    private suspend fun handleAndroidTask(
        input: String,
        intent: DetectedIntent,
        userName: String
    ): OrchestratorResponse {
        val task = taskPlanner.planTask(input, intent)
        return OrchestratorResponse(
            text = "On it, $userName. Opening ${intent.appTarget ?: "the app"} now.",
            taskCreated = task
        )
    }

    private suspend fun handlePhoneCommand(intent: DetectedIntent, userName: String): OrchestratorResponse {
        val result = androidController.executeSystemAction(intent.action ?: "")
        return if (result.success) {
            OrchestratorResponse("Done, $userName.")
        } else {
            OrchestratorResponse(result.message, isError = true)
        }
    }

    private suspend fun handleKnowledgeQuestion(input: String): OrchestratorResponse {
        val wikiResult = wikiProvider.search(input)
        return if (wikiResult != null) {
            OrchestratorResponse(wikiResult)
        } else {
            handleWithAI(input)
        }
    }

    private suspend fun handleWithAI(
        input: String,
        systemOverride: String? = null,
        additionalContext: String? = null
    ): OrchestratorResponse {
        val provider = modelRouter.getActiveProvider()
            ?: return OrchestratorResponse(
                "No AI model is configured. Please go to Settings → AI Models and add an API key.",
                isError = true
            )

        val system = systemOverride ?: systemPrompt
        val memContext = memoryRepository.buildMemoryContext()
        val contextBlock = buildString {
            if (memContext.isNotBlank()) append("User memory context:\n$memContext\n\n")
            if (additionalContext != null) append("$additionalContext\n\n")
        }

        val messages = mutableListOf(AIMessage("system", system))
        if (contextBlock.isNotBlank()) messages.add(AIMessage("system", contextBlock.trim()))
        messages.add(AIMessage("user", input))

        val response = provider.chat(messages)
        return if (response.isError) {
            OrchestratorResponse(
                "I couldn't connect to the AI model. ${response.errorMessage}",
                isError = true
            )
        } else {
            OrchestratorResponse(response.text)
        }
    }
}
