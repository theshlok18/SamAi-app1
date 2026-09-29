package dev.shlok.samai.ui.screens.tasks

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shlok.samai.tasks.*
import dev.shlok.samai.ui.components.GlassCard
import dev.shlok.samai.ui.components.GlowingDivider
import dev.shlok.samai.ui.theme.*

@Composable
fun TasksScreen(
    onNavigateBack: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val activeTask by viewModel.activeTask.collectAsStateWithLifecycle()
    val taskHistory by viewModel.taskHistory.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF060D1A), SamBackground)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SamTextSecondary)
                }
                Text("Task Center", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SamCyan)
            }
            GlowingDivider()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                // Active task
                activeTask?.let { task ->
                    item { Text("ACTIVE TASK", fontSize = 11.sp, color = SamTextDim, letterSpacing = 2.sp) }
                    item { ActiveTaskCard(task = task, onCancel = viewModel::cancelTask) }
                }

                // History
                if (taskHistory.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text("TASK HISTORY", fontSize = 11.sp, color = SamTextDim, letterSpacing = 2.sp)
                    }
                    items(taskHistory) { task ->
                        TaskHistoryCard(task = task)
                    }
                }

                if (activeTask == null && taskHistory.isEmpty()) {
                    item { EmptyTaskState() }
                }
            }
        }
    }
}

@Composable
private fun ActiveTaskCard(task: Task, onCancel: () -> Unit) {
    GlassCard(borderColor = SamCyan.copy(0.5f)) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TASK RUNNING",
                        fontSize = 10.sp,
                        color = SamSuccess,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = task.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SamTextPrimary
                    )
                }
                val infiniteTransition = rememberInfiniteTransition(label = "spinner")
                val rotation by infiniteTransition.animateFloat(
                    0f, 360f,
                    infiniteRepeatable(tween(1500, easing = LinearEasing)),
                    label = "spin"
                )
                CircularProgressIndicator(
                    progress = { task.progress },
                    modifier = Modifier.size(44.dp),
                    color = SamCyan,
                    trackColor = SamCyan.copy(0.1f),
                    strokeWidth = 3.dp
                )
            }

            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { task.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = SamCyan,
                trackColor = SamCyan.copy(0.15f)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${task.completedSteps}/${task.totalSteps} steps",
                fontSize = 11.sp,
                color = SamTextSecondary
            )

            Spacer(Modifier.height(12.dp))
            task.steps.forEach { step -> StepRow(step) }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SamError),
                border = BorderStroke(1.dp, SamError.copy(0.4f))
            ) {
                Text("Cancel Task")
            }
        }
    }
}

@Composable
private fun StepRow(step: TaskStep) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(
                    when (step.state) {
                        StepState.COMPLETED -> SamSuccess.copy(0.2f)
                        StepState.RUNNING -> SamCyan.copy(0.2f)
                        StepState.FAILED -> SamError.copy(0.2f)
                        else -> SamSurface
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (step.state) {
                StepState.COMPLETED -> Icon(
                    Icons.Filled.Check, null,
                    tint = SamSuccess, modifier = Modifier.size(12.dp)
                )
                StepState.RUNNING -> {
                    val inf = rememberInfiniteTransition(label = "stepDot")
                    val a by inf.animateFloat(
                        0.3f, 1f,
                        infiniteRepeatable(tween(500), RepeatMode.Reverse),
                        label = "a"
                    )
                    Box(Modifier.size(6.dp).background(SamCyan.copy(a), CircleShape))
                }
                StepState.FAILED -> Icon(
                    Icons.Filled.Close, null,
                    tint = SamError, modifier = Modifier.size(12.dp)
                )
                else -> Box(Modifier.size(6.dp).background(SamTextDim, CircleShape))
            }
        }
        Text(
            text = step.description,
            fontSize = 13.sp,
            color = when (step.state) {
                StepState.COMPLETED -> SamTextSecondary
                StepState.RUNNING -> SamTextPrimary
                StepState.FAILED -> SamError
                else -> SamTextDim
            }
        )
    }
}

@Composable
private fun TaskHistoryCard(task: Task) {
    GlassCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        when (task.state) {
                            TaskState.COMPLETED -> SamSuccess.copy(0.15f)
                            TaskState.FAILED -> SamError.copy(0.15f)
                            TaskState.CANCELLED -> SamWarning.copy(0.15f)
                            else -> SamSurface
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (task.state) {
                        TaskState.COMPLETED -> Icons.Filled.Check
                        TaskState.FAILED -> Icons.Filled.Close
                        TaskState.CANCELLED -> Icons.Filled.Cancel
                        else -> Icons.Filled.HourglassEmpty
                    },
                    contentDescription = null,
                    tint = when (task.state) {
                        TaskState.COMPLETED -> SamSuccess
                        TaskState.FAILED -> SamError
                        else -> SamWarning
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(task.title, fontSize = 14.sp, color = SamTextPrimary, fontWeight = FontWeight.Medium)
                Text(
                    "${task.completedSteps}/${task.totalSteps} steps",
                    fontSize = 11.sp,
                    color = SamTextSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyTaskState() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Filled.PlayArrow,
                null,
                tint = SamTextDim,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text("No tasks yet", fontSize = 16.sp, color = SamTextSecondary)
            Text(
                "Say \"Sam, open YouTube\" to create a task",
                fontSize = 12.sp,
                color = SamTextDim
            )
        }
    }
}
