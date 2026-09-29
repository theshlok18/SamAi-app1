package dev.shlok.samai.ui.screens.coding

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.shlok.samai.ui.components.GlassCard
import dev.shlok.samai.ui.components.GlowingDivider
import dev.shlok.samai.ui.theme.*

@Composable
fun CodingScreen(
    onNavigateBack: () -> Unit,
    viewModel: CodingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Editor", "Preview", "Projects")

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
            // Header
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Filled.ArrowBack, "Back", tint = SamTextSecondary)
                }
                Text("Coding Studio", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SamCyan)
            }

            GlowingDivider()

            // Prompt bar
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.prompt,
                    onValueChange = viewModel::updatePrompt,
                    placeholder = { Text("Describe what to build...", color = SamTextDim, fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
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
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Button(
                    onClick = viewModel::generateCode,
                    enabled = !uiState.isGenerating && uiState.prompt.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = SamCyan.copy(0.15f)),
                    border = BorderStroke(1.dp, SamCyan.copy(0.6f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = SamCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.AutoFixHigh, null, tint = SamCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (uiState.error.isNotBlank()) {
                Text(
                    uiState.error,
                    color = SamError,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = SamCyan,
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[selectedTab])
                                .height(2.dp)
                                .background(SamCyan)
                        )
                    }
                }
            ) {
                tabs.forEachIndexed { i, title ->
                    Tab(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        text = {
                            Text(
                                title,
                                fontSize = 12.sp,
                                color = if (selectedTab == i) SamCyan else SamTextSecondary
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                0 -> EditorTab(
                    project = uiState.activeProject,
                    onCodeChange = viewModel::updateCode,
                    onExplain = viewModel::aiExplain,
                    onFix = viewModel::aiFix,
                    onPreview = { selectedTab = 1 },
                    output = uiState.output
                )
                1 -> PreviewTab(uiState.activeProject)
                2 -> ProjectsTab(
                    projects = uiState.projects,
                    onSelect = { viewModel.selectProject(it); selectedTab = 0 }
                )
            }
        }
    }
}

@Composable
private fun EditorTab(
    project: CodeProject?,
    onCodeChange: (String) -> Unit,
    onExplain: () -> Unit,
    onFix: () -> Unit,
    onPreview: () -> Unit,
    output: String
) {
    Column(Modifier.fillMaxSize()) {
        if (project != null) {
            // Toolbar
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ToolbarButton("Explain", Icons.Filled.Info, onExplain)
                ToolbarButton("Fix", Icons.Filled.Build, onFix)
                if (project.isHtml) {
                    ToolbarButton("Preview", Icons.Filled.Visibility, onPreview)
                }
            }
            // Code editor
            OutlinedTextField(
                value = project.code,
                onValueChange = onCodeChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SamCyan.copy(0.3f),
                    unfocusedBorderColor = SamCyan.copy(0.1f),
                    focusedTextColor = Color(0xFF9EDBFF),
                    unfocusedTextColor = Color(0xFF9EDBFF),
                    cursorColor = SamCyan,
                    focusedContainerColor = Color(0xFF050D18),
                    unfocusedContainerColor = Color(0xFF050D18)
                ),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                ),
                shape = RoundedCornerShape(8.dp)
            )

            if (output.isNotBlank()) {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Output", fontSize = 11.sp, color = SamTextDim, letterSpacing = 2.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(output, fontSize = 13.sp, color = SamTextPrimary)
                    }
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Code, null, tint = SamTextDim, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Describe what to build above", color = SamTextSecondary, fontSize = 14.sp)
                    Text("Example: \"Create a Python calculator\"", color = SamTextDim, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ToolbarButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = SamCyan),
        border = BorderStroke(1.dp, SamCyan.copy(0.4f)),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 11.sp)
    }
}

@Composable
private fun PreviewTab(project: CodeProject?) {
    if (project?.isHtml == true) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, project.code, "text/html", "UTF-8", null)
            },
            modifier = Modifier.fillMaxSize().padding(8.dp)
        )
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Web, null, tint = SamTextDim, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(12.dp))
                Text(
                    if (project == null) "Generate HTML/CSS/JS to see preview"
                    else "Preview only available for HTML projects",
                    color = SamTextSecondary,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun ProjectsTab(projects: List<CodeProject>, onSelect: (CodeProject) -> Unit) {
    if (projects.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No projects yet", color = SamTextSecondary)
        }
    } else {
        LazyColumn(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(projects) { project ->
                GlassCard(onClick = { onSelect(project) }) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Code, null, tint = SamCyan, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(project.name, fontSize = 14.sp, color = SamTextPrimary, fontWeight = FontWeight.Medium)
                            Text(project.language, fontSize = 11.sp, color = SamTextSecondary)
                        }
                    }
                }
            }
        }
    }
}
