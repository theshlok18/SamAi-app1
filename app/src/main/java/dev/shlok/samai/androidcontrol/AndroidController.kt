package dev.shlok.samai.androidcontrol

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidController @Inject constructor(
    @ApplicationContext private val context: Context,
    val appLauncher: AppLauncher
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun executeSystemAction(action: String): ActionResult {
        val service = SamAccessibilityService.instance.value

        return when (action.lowercase()) {
            "back" -> service?.let {
                if (it.performGlobalBack()) ActionResult(true, "Went back.")
                else ActionResult(false, "Could not go back.")
            } ?: accessibilityNotConnected()

            "home" -> service?.let {
                if (it.performGlobalHome()) ActionResult(true, "Went to home screen.")
                else ActionResult(false, "Could not go home.")
            } ?: accessibilityNotConnected()

            "recent", "recents" -> service?.let {
                if (it.performGlobalRecents()) ActionResult(true, "Opened recent apps.")
                else ActionResult(false, "Could not open recents.")
            } ?: accessibilityNotConnected()

            "scroll_down", "scroll" -> service?.let {
                if (it.scrollDown()) ActionResult(true, "Scrolled down.")
                else ActionResult(false, "Could not scroll.")
            } ?: accessibilityNotConnected()

            "scroll_up" -> service?.let {
                if (it.scrollUp()) ActionResult(true, "Scrolled up.")
                else ActionResult(false, "Could not scroll up.")
            } ?: accessibilityNotConnected()

            "volume_up" -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                )
                ActionResult(true, "Volume increased.")
            }

            "volume_down" -> {
                audioManager.adjustStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                )
                ActionResult(true, "Volume decreased.")
            }

            "notifications" -> service?.let {
                if (it.performGlobalNotifications()) ActionResult(true, "Opened notifications.")
                else ActionResult(false, "Could not open notifications.")
            } ?: accessibilityNotConnected()

            else -> ActionResult(false, "Unknown action: $action")
        }
    }

    fun openApp(appName: String): ActionResult = appLauncher.launchApp(appName)

    fun scrollDown(times: Int = 1): ActionResult {
        val service = SamAccessibilityService.instance.value
            ?: return accessibilityNotConnected()
        repeat(times) {
            service.scrollDown()
            Thread.sleep(500)
        }
        return ActionResult(true, "Scrolled down ${times}x.")
    }

    fun tapText(text: String): ActionResult {
        val service = SamAccessibilityService.instance.value
            ?: return accessibilityNotConnected()
        return if (service.findAndClickNodeWithText(text)) {
            ActionResult(true, "Tapped '$text'.")
        } else {
            ActionResult(false, "Could not find '$text' on screen.")
        }
    }

    fun typeText(text: String): ActionResult {
        val service = SamAccessibilityService.instance.value
            ?: return accessibilityNotConnected()
        return if (service.typeText(text)) {
            ActionResult(true, "Typed text.")
        } else {
            ActionResult(false, "No text field is focused.")
        }
    }

    fun isAccessibilityConnected() = SamAccessibilityService.isConnected()

    private fun accessibilityNotConnected() = ActionResult(
        false,
        "SAM Accessibility Service is not enabled. Please enable it in Settings."
    )
}
