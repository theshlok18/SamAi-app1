package dev.shlok.samai.ui.screens.tasks

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.shlok.samai.tasks.Task
import dev.shlok.samai.tasks.TaskExecutor
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskExecutor: TaskExecutor
) : ViewModel() {
    val activeTask: StateFlow<Task?> = taskExecutor.activeTask
    val taskHistory: StateFlow<List<Task>> = taskExecutor.taskHistory

    fun cancelTask() = taskExecutor.cancelTask()
}
