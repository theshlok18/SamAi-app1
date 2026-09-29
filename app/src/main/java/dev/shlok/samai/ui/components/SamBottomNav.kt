package dev.shlok.samai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.shlok.samai.ui.theme.*

data class NavItem(val label: String, val icon: ImageVector, val route: String)

val navItems = listOf(
    NavItem("Home", Icons.Filled.Home, "home"),
    NavItem("Chat", Icons.Filled.Chat, "chat"),
    NavItem("Tasks", Icons.Filled.PlayArrow, "tasks"),
    NavItem("Code", Icons.Filled.Code, "coding"),
    NavItem("Settings", Icons.Filled.Settings, "settings")
)

@Composable
fun SamBottomNav(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF080F1E), Color(0xFF0A1225), Color(0xFF080F1E))
                )
            )
            .border(
                1.dp,
                SamCyan.copy(0.2f),
                RoundedCornerShape(24.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            navItems.forEach { item ->
                NavTabItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) }
                )
            }
        }
    }
}

@Composable
private fun NavTabItem(
    item: NavItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "navScale"
    )
    val color = if (isSelected) SamCyan else SamTextSecondary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            fontSize = 9.sp,
            color = color,
            letterSpacing = 0.5.sp
        )
        if (isSelected) {
            Spacer(Modifier.height(3.dp))
            Box(
                Modifier
                    .width(16.dp)
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(listOf(SamCyan.copy(0f), SamCyan, SamCyan.copy(0f))),
                        RoundedCornerShape(1.dp)
                    )
            )
        }
    }
}
