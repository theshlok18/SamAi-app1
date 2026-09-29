package dev.shlok.samai.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.shlok.samai.ui.navigation.SamRoute
import dev.shlok.samai.ui.screens.chat.ChatScreen
import dev.shlok.samai.ui.screens.coding.CodingScreen
import dev.shlok.samai.ui.screens.home.HomeScreen
import dev.shlok.samai.ui.screens.memory.MemoryScreen
import dev.shlok.samai.ui.screens.settings.AccessibilitySetupScreen
import dev.shlok.samai.ui.screens.settings.AIModelSettingsScreen
import dev.shlok.samai.ui.screens.settings.SettingsScreen
import dev.shlok.samai.ui.screens.splash.SplashScreen
import dev.shlok.samai.ui.screens.tasks.TasksScreen
import dev.shlok.samai.ui.theme.SamBackground

@Composable
fun SamApp(onSplashReady: () -> Unit) {
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SamBackground)
    ) {
        NavHost(
            navController = navController,
            startDestination = SamRoute.Splash.route,
            enterTransition = {
                fadeIn(animationSpec = tween(400)) + slideInHorizontally(
                    animationSpec = tween(400)
                ) { it / 8 }
            },
            exitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    animationSpec = tween(300)
                ) { -it / 8 }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(400)) + slideInHorizontally(
                    animationSpec = tween(400)
                ) { -it / 8 }
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                    animationSpec = tween(300)
                ) { it / 8 }
            }
        ) {
            composable(SamRoute.Splash.route) {
                SplashScreen(
                    onSplashReady = onSplashReady,
                    onNavigateToHome = {
                        navController.navigate(SamRoute.Home.route) {
                            popUpTo(SamRoute.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(SamRoute.Home.route) {
                HomeScreen(
                    onNavigateToChat = { navController.navigate(SamRoute.Chat.route) },
                    onNavigateToTasks = { navController.navigate(SamRoute.Tasks.route) },
                    onNavigateToCoding = { navController.navigate(SamRoute.Coding.route) },
                    onNavigateToMemory = { navController.navigate(SamRoute.Memory.route) },
                    onNavigateToSettings = { navController.navigate(SamRoute.Settings.route) }
                )
            }
            composable(SamRoute.Chat.route) {
                ChatScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToTasks = { navController.navigate(SamRoute.Tasks.route) }
                )
            }
            composable(SamRoute.Tasks.route) {
                TasksScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(SamRoute.Coding.route) {
                CodingScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(SamRoute.Memory.route) {
                MemoryScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(SamRoute.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToAccessibility = { navController.navigate(SamRoute.AccessibilitySetup.route) },
                    onNavigateToAIModels = { navController.navigate(SamRoute.AIModelSettings.route) }
                )
            }
            composable(SamRoute.AccessibilitySetup.route) {
                AccessibilitySetupScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(SamRoute.AIModelSettings.route) {
                AIModelSettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
