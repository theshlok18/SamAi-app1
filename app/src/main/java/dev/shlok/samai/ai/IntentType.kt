package dev.shlok.samai.ai

enum class IntentType {
    CONVERSATION,
    KNOWLEDGE_QUESTION,
    PHONE_COMMAND,
    APP_AUTOMATION,
    CODING_REQUEST,
    MEMORY_REQUEST,
    SETTINGS_REQUEST,
    DEVELOPER_INFO,
    TASK_EXECUTION
}

data class DetectedIntent(
    val type: IntentType,
    val appTarget: String? = null,
    val action: String? = null,
    val parameters: Map<String, String> = emptyMap(),
    val rawQuery: String = ""
)

object IntentDetector {

    private val appLaunchPatterns = listOf(
        Regex("(?:open|launch|start|run)\\s+(\\w+)", RegexOption.IGNORE_CASE),
        Regex("(?:go to|navigate to)\\s+(\\w+)", RegexOption.IGNORE_CASE)
    )

    private val phoneCommandPatterns = mapOf(
        "back" to Regex("(?:go back|press back|back button)", RegexOption.IGNORE_CASE),
        "home" to Regex("(?:go home|press home|home screen|home button)", RegexOption.IGNORE_CASE),
        "recent" to Regex("(?:recent apps|open recent|show recent|recents)", RegexOption.IGNORE_CASE),
        "volume_up" to Regex("(?:volume up|increase volume|louder)", RegexOption.IGNORE_CASE),
        "volume_down" to Regex("(?:volume down|decrease volume|quieter|softer)", RegexOption.IGNORE_CASE),
        "screenshot" to Regex("(?:take screenshot|capture screen)", RegexOption.IGNORE_CASE),
        "scroll_up" to Regex("(?:scroll up|swipe up)", RegexOption.IGNORE_CASE),
        "scroll_down" to Regex("(?:scroll down|swipe down)", RegexOption.IGNORE_CASE)
    )

    private val memoryPatterns = listOf(
        Regex("remember\\s+(?:that\\s+)?(?:my\\s+)?(.+)", RegexOption.IGNORE_CASE),
        Regex("(?:my|i)\\s+\\w+\\s+is\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("what\\s+(?:is|are|do you know about)\\s+my\\s+(.+)", RegexOption.IGNORE_CASE)
    )

    private val codingPatterns = listOf(
        Regex("(?:create|write|build|generate|code)\\s+(?:a|an)?\\s*(?:\\w+\\s+)?(?:code|program|script|function|app|website|html|python|java|kotlin)", RegexOption.IGNORE_CASE),
        Regex("(?:fix|debug|explain)\\s+(?:this\\s+)?code", RegexOption.IGNORE_CASE)
    )

    private val knowledgePatterns = listOf(
        Regex("(?:what is|what are|tell me about|explain|search|wiki|wikipedia)\\s+(.+)", RegexOption.IGNORE_CASE),
        Regex("(?:who is|who was|who are)\\s+(.+)", RegexOption.IGNORE_CASE)
    )

    fun detect(query: String): DetectedIntent {
        val lower = query.lowercase().trim()

        // Developer info check
        if (dev.shlok.samai.profile.DeveloperProfile.isDevQuery(lower)) {
            return DetectedIntent(IntentType.DEVELOPER_INFO, rawQuery = query)
        }

        // Memory requests
        if (lower.startsWith("remember") || lower.contains("don't forget") ||
            lower.contains("what is my ") || lower.contains("what's my ")) {
            return DetectedIntent(IntentType.MEMORY_REQUEST, rawQuery = query)
        }

        // Phone system commands
        for ((action, pattern) in phoneCommandPatterns) {
            if (pattern.containsMatchIn(lower)) {
                return DetectedIntent(IntentType.PHONE_COMMAND, action = action, rawQuery = query)
            }
        }

        // App launch + possible multi-step
        for (pattern in appLaunchPatterns) {
            val match = pattern.find(lower)
            if (match != null) {
                val appName = match.groupValues[1]
                val hasMultiStep = lower.contains(" and ") || lower.contains(" then ")
                return DetectedIntent(
                    type = if (hasMultiStep) IntentType.TASK_EXECUTION else IntentType.APP_AUTOMATION,
                    appTarget = appName,
                    rawQuery = query
                )
            }
        }

        // Scroll/swipe without app prefix
        if (lower.contains("scroll") || lower.contains("swipe")) {
            return DetectedIntent(IntentType.PHONE_COMMAND, action = "scroll", rawQuery = query)
        }

        // Coding
        if (codingPatterns.any { it.containsMatchIn(lower) }) {
            return DetectedIntent(IntentType.CODING_REQUEST, rawQuery = query)
        }

        // Knowledge
        if (knowledgePatterns.any { it.containsMatchIn(lower) }) {
            return DetectedIntent(IntentType.KNOWLEDGE_QUESTION, rawQuery = query)
        }

        // Settings
        if (lower.contains("setting") || lower.contains("configure") || lower.contains("setup")) {
            return DetectedIntent(IntentType.SETTINGS_REQUEST, rawQuery = query)
        }

        return DetectedIntent(IntentType.CONVERSATION, rawQuery = query)
    }
}
