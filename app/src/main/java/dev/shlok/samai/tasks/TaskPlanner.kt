package dev.shlok.samai.tasks

import dev.shlok.samai.ai.DetectedIntent
import dev.shlok.samai.ai.IntentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskPlanner @Inject constructor() {

    fun planTask(command: String, intent: DetectedIntent): Task {
        val lower = command.lowercase()
        val steps = mutableListOf<TaskStep>()
        val title = buildTitle(intent)

        when {
            // Multi-step: open app + navigate/action
            lower.contains(" and ") || lower.contains(" then ") -> {
                planMultiStepTask(lower, intent, steps)
            }
            // Single app launch
            intent.type == IntentType.APP_AUTOMATION && intent.appTarget != null -> {
                steps.add(TaskStep(
                    description = "Launch ${intent.appTarget}",
                    action = TaskAction.LaunchApp(intent.appTarget)
                ))
                steps.add(TaskStep(
                    description = "Wait for ${intent.appTarget} to load",
                    action = TaskAction.Wait(2000)
                ))
                steps.add(TaskStep(
                    description = "Verify ${intent.appTarget} launched",
                    action = TaskAction.Wait(500)
                ))
            }
            else -> {
                // Generic attempt
                intent.appTarget?.let { app ->
                    steps.add(TaskStep(description = "Launch $app", action = TaskAction.LaunchApp(app)))
                    steps.add(TaskStep(description = "Wait for load", action = TaskAction.Wait(2000)))
                }
            }
        }

        return Task(
            title = title,
            originalCommand = command,
            steps = steps,
            state = TaskState.PENDING
        )
    }

    private fun planMultiStepTask(
        lower: String,
        intent: DetectedIntent,
        steps: MutableList<TaskStep>
    ) {
        val app = intent.appTarget ?: return
        // Launch app first
        steps.add(TaskStep(description = "Launch $app", action = TaskAction.LaunchApp(app)))
        steps.add(TaskStep(description = "Wait for $app to load", action = TaskAction.Wait(2500)))

        // Detect secondary actions
        when {
            lower.contains("reels") && app == "instagram" -> {
                steps.add(TaskStep(description = "Navigate to Reels", action = TaskAction.TapText("Reels")))
                steps.add(TaskStep(description = "Wait for Reels", action = TaskAction.Wait(1500)))
                extractScrollCount(lower)?.let { count ->
                    repeat(count) { i ->
                        steps.add(TaskStep(
                            description = "Scroll reel ${i + 1}/$count",
                            action = TaskAction.Scroll("up", 1)
                        ))
                        steps.add(TaskStep(description = "Pause", action = TaskAction.Wait(800)))
                    }
                }
            }

            lower.contains("search") -> {
                val searchQuery = extractSearchQuery(lower)
                steps.add(TaskStep(description = "Tap search", action = TaskAction.TapText("Search")))
                steps.add(TaskStep(description = "Wait", action = TaskAction.Wait(1000)))
                if (searchQuery.isNotBlank()) {
                    steps.add(TaskStep(description = "Type search query", action = TaskAction.TypeText(searchQuery)))
                }
            }

            lower.contains("message") || lower.contains("send") -> {
                val contact = extractContactName(lower)
                steps.add(TaskStep(description = "Open search", action = TaskAction.TapText("Search")))
                if (contact.isNotBlank()) {
                    steps.add(TaskStep(description = "Search for $contact", action = TaskAction.TypeText(contact)))
                    steps.add(TaskStep(description = "Wait for results", action = TaskAction.Wait(1500)))
                    steps.add(TaskStep(description = "Open chat with $contact", action = TaskAction.TapText(contact)))
                }
            }

            lower.contains("navigate") || lower.contains("directions") -> {
                val destination = extractDestination(lower)
                if (destination.isNotBlank()) {
                    steps.add(TaskStep(description = "Search for $destination", action = TaskAction.TapText("Search here")))
                    steps.add(TaskStep(description = "Type destination", action = TaskAction.TypeText(destination)))
                }
            }

            lower.contains("scroll") -> {
                val count = extractScrollCount(lower) ?: 3
                repeat(count) { i ->
                    steps.add(TaskStep(
                        description = "Scroll ${i + 1}/$count",
                        action = TaskAction.Scroll("down", 1)
                    ))
                    steps.add(TaskStep(description = "Pause", action = TaskAction.Wait(500)))
                }
            }
        }
    }

    private fun buildTitle(intent: DetectedIntent): String {
        val app = intent.appTarget?.replaceFirstChar { it.uppercase() } ?: "Task"
        return "Open $app"
    }

    private fun extractScrollCount(lower: String): Int? {
        val match = Regex("(\\d+)\\s*(?:times?|reels?|scroll)").find(lower)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun extractSearchQuery(lower: String): String {
        return Regex("search(?:ing)?\\s+(?:for\\s+)?(.+?)(?:\\s+on|$)").find(lower)
            ?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractContactName(lower: String): String {
        return Regex("(?:message|to|for)\\s+(\\w+)").find(lower)
            ?.groupValues?.get(1)?.trim() ?: ""
    }

    private fun extractDestination(lower: String): String {
        return Regex("(?:navigate to|directions to|go to)\\s+(.+)").find(lower)
            ?.groupValues?.get(1)?.trim() ?: ""
    }
}
