package dev.shlok.samai.ui.navigation

sealed class SamRoute(val route: String) {
    object Splash : SamRoute("splash")
    object Home : SamRoute("home")
    object Chat : SamRoute("chat")
    object Tasks : SamRoute("tasks")
    object Coding : SamRoute("coding")
    object Memory : SamRoute("memory")
    object Knowledge : SamRoute("knowledge")
    object Settings : SamRoute("settings")
    object AccessibilitySetup : SamRoute("accessibility_setup")
    object AIModelSettings : SamRoute("ai_model_settings")
}
