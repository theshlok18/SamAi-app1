package dev.shlok.samai.tasks

import java.util.UUID

enum class TaskState {
    PENDING, PLANNING, RUNNING, WAITING_FOR_PERMISSION,
    WAITING_FOR_CONFIRMATION, COMPLETED, FAILED, CANCELLED
}

enum class StepState { PENDING, RUNNING, COMPLETED, FAILED, SKIPPED }

data class TaskStep(
    val id: String = UUID.randomUUID().toString(),
    val description: String,
    val action: TaskAction,
    var state: StepState = StepState.PENDING,
    var errorMessage: String = ""
)

sealed class TaskAction {
    data class LaunchApp(val appName: String) : TaskAction()
    data class SystemAction(val action: String) : TaskAction()
    data class TapText(val text: String) : TaskAction()
    data class TypeText(val text: String) : TaskAction()
    data class Scroll(val direction: String = "down", val times: Int = 1) : TaskAction()
    data class Wait(val millis: Long = 1500) : TaskAction()
    data class OpenUrl(val url: String) : TaskAction()
    data class AIQuery(val prompt: String) : TaskAction()
    object GoHome : TaskAction()
    object GoBack : TaskAction()
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val originalCommand: String,
    val steps: MutableList<TaskStep> = mutableListOf(),
    var state: TaskState = TaskState.PENDING,
    var currentStepIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    var completedAt: Long? = null,
    var errorMessage: String = ""
) {
    val completedSteps get() = steps.count { it.state == StepState.COMPLETED }
    val totalSteps get() = steps.size
    val progress get() = if (totalSteps == 0) 0f else completedSteps.toFloat() / totalSteps
    val currentStep get() = steps.getOrNull(currentStepIndex)
}
