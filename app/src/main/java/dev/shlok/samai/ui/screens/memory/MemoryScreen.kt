package dev.shlok.samai.ui.screens.memory

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shlok.samai.ui.components.GlassCard
import dev.shlok.samai.ui.components.GlowingDivider
import dev.shlok.samai.ui.theme.*

@Composable
fun MemoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showClearDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF060D1A), SamBackground)))
    ) {
        Column(Modifier.fillMaxSize().systemBarsPadding()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SamTextSecondary)
                }
                Text("Memory", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SamCyan)
                Spacer(Modifier.weight(1f))
                if (uiState.memories.isNotEmpty()) {
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Filled.DeleteSweep, "Clear All", tint = SamError.copy(0.7f))
                    }
                }
                IconButton(onClick = viewModel::showAddDialog) {
                    Icon(Icons.Filled.Add, "Add Memory", tint = SamCyan)
                }
            }

            GlowingDivider()

            // Search
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::search,
                placeholder = { Text("Search memories...", color = SamTextDim, fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = SamTextSecondary) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SamCyan.copy(0.6f),
                    unfocusedBorderColor = SamCyan.copy(0.2f),
                    focusedTextColor = SamTextPrimary,
                    unfocusedTextColor = SamTextPrimary,
                    cursorColor = SamCyan,
                    focusedContainerColor = SamSurface,
                    unfocusedContainerColor = SamSurface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            if (uiState.memories.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Psychology, null, tint = SamTextDim, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No memories yet", color = SamTextSecondary, fontSize = 14.sp)
                        Text(
                            "Say \"Sam, remember that my favorite language is Python\"",
                            color = SamTextDim, fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.memories) { memory ->
                        GlassCard {
                            Row(
                                Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Filled.Circle,
                                    null,
                                    tint = SamCyan.copy(0.6f),
                                    modifier = Modifier.size(8.dp).padding(top = 6.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        memory.key,
                                        fontSize = 11.sp,
                                        color = SamCyan,
                                        letterSpacing = 0.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(memory.value, fontSize = 14.sp, color = SamTextPrimary)
                                }
                                Row {
                                    IconButton(
                                        onClick = { viewModel.showEditDialog(memory) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Edit, null,
                                            tint = SamTextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteMemory(memory) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete, null,
                                            tint = SamError.copy(0.6f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (uiState.showAddDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            containerColor = SamSurface,
            titleContentColor = SamTextPrimary,
            title = {
                Text(
                    if (uiState.editingMemory == null) "Add Memory" else "Edit Memory",
                    color = SamCyan
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.newKey,
                        onValueChange = viewModel::updateNewKey,
                        label = { Text("Key (e.g. favorite language)", color = SamTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SamCyan.copy(0.6f),
                            unfocusedBorderColor = SamCyan.copy(0.2f),
                            focusedTextColor = SamTextPrimary,
                            unfocusedTextColor = SamTextPrimary,
                            cursorColor = SamCyan
                        )
                    )
                    OutlinedTextField(
                        value = uiState.newValue,
                        onValueChange = viewModel::updateNewValue,
                        label = { Text("Value", color = SamTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SamCyan.copy(0.6f),
                            unfocusedBorderColor = SamCyan.copy(0.2f),
                            focusedTextColor = SamTextPrimary,
                            unfocusedTextColor = SamTextPrimary,
                            cursorColor = SamCyan
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = viewModel::saveMemory,
                    colors = ButtonDefaults.buttonColors(containerColor = SamCyan.copy(0.2f))
                ) {
                    Text("Save", color = SamCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialog) {
                    Text("Cancel", color = SamTextSecondary)
                }
            }
        )
    }

    // Clear confirmation
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = SamSurface,
            title = { Text("Clear All Memories?", color = SamError) },
            text = { Text("This will delete all stored memories permanently.", color = SamTextSecondary) },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAll(); showClearDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SamError.copy(0.2f))
                ) {
                    Text("Clear All", color = SamError)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = SamTextSecondary)
                }
            }
        )
    }
}
