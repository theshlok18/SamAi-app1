package dev.shlok.samai.tasks

import dev.shlok.samai.androidcontrol.AndroidController
import dev.shlok.samai.androidcontrol.AppLauncher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskExecutor @Inject constructor(
    private val androidController: AndroidController
) {
    private val _activeTask = MutableStateFlow<Task?>(null)
    val activeTask: StateFlow<Task?> = _activeTask

    private val _taskHistory = MutableStateFlow<List<Task>>(emptyList())
    val taskHistory: StateFlow<List<Task>> = _taskHistory

    suspend fun execute(task: Task, onUpdate: (Task) -> Unit) {
        _activeTask.value = task
        task.state = TaskState.RUNNING
        onUpdate(task)

        for ((index, step) in task.steps.withIndex()) {
            task.currentStepIndex = index
            step.state = StepState.RUNNING
            onUpdate(task)

            val result = executeStep(step)

            if (result) {
                step.state = StepState.COMPLETED
            } else {
                step.state = StepState.FAILED
                step.errorMessage = "Step failed"
                // For non-critical steps (like wait), continue; for critical steps, fail task
                if (step.action is TaskAction.LaunchApp) {
                    task.state = TaskState.FAILED
                    task.errorMessage = "Could not launch app."
                    onUpdate(task)
                    finalize(task)
                    return
                }
            }
            onUpdate(task)
        }

        task.state = TaskState.COMPLETED
        task.completedAt = System.currentTimeMillis()
        onUpdate(task)
        finalize(task)
    }

    private suspend fun executeStep(step: TaskStep): Boolean {
        return when (val action = step.action) {
            is TaskAction.LaunchApp -> {
                val result = androidController.openApp(action.appName)
                result.success
            }
            is TaskAction.SystemAction -> {
                androidController.executeSystemAction(action.action).success
            }
            is TaskAction.TapText -> {
                androidController.tapText(action.text).success
            }
            is TaskAction.TypeText -> {
                androidController.typeText(action.text).success
            }
            is TaskAction.Scroll -> {
                androidController.scrollDown(action.times).success
            }
            is TaskAction.Wait -> {
                delay(action.millis)
                true
            }
            is TaskAction.OpenUrl -> {
                androidController.appLauncher.openUrl(action.url).success
            }
            is TaskAction.AIQuery -> true // handled at orchestrator level
            TaskAction.GoHome -> androidController.executeSystemAction("home").success
            TaskAction.GoBack -> androidController.executeSystemAction("back").success
        }
    }

    fun cancelTask() {
        _activeTask.value?.let { task ->
            task.state = TaskState.CANCELLED
            finalize(task)
        }
    }

    private fun finalize(task: Task) {
        _taskHistory.value = listOf(task) + _taskHistory.value.take(49)
        _activeTask.value = null
    }
}
