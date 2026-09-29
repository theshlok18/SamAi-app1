package dev.shlok.samai.androidcontrol

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class ActionResult(val success: Boolean, val message: String = "")

@Singleton
class AppLauncher @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager: PackageManager = context.packageManager

    // Common app package name aliases
    private val appAliases = mapOf(
        "youtube" to "com.google.android.youtube",
        "instagram" to "com.instagram.android",
        "whatsapp" to "com.whatsapp",
        "telegram" to "org.telegram.messenger",
        "chrome" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "camera" to "com.android.camera2",
        "settings" to "com.android.settings",
        "facebook" to "com.facebook.katana",
        "twitter" to "com.twitter.android",
        "x" to "com.twitter.android",
        "spotify" to "com.spotify.music",
        "netflix" to "com.netflix.mediaclient",
        "photos" to "com.google.android.apps.photos",
        "google photos" to "com.google.android.apps.photos",
        "calculator" to "com.android.calculator2",
        "calendar" to "com.google.android.calendar",
        "clock" to "com.android.deskclock",
        "contacts" to "com.android.contacts",
        "dialer" to "com.android.dialer",
        "phone" to "com.android.dialer",
        "messages" to "com.google.android.apps.messaging",
        "sms" to "com.google.android.apps.messaging",
        "files" to "com.google.android.documentsui",
        "play store" to "com.android.vending",
        "playstore" to "com.android.vending",
        "drive" to "com.google.android.apps.docs",
        "google drive" to "com.google.android.apps.docs",
        "docs" to "com.google.android.apps.docs.editors.docs",
        "sheets" to "com.google.android.apps.docs.editors.sheets",
        "slides" to "com.google.android.apps.docs.editors.slides",
        "meet" to "com.google.android.apps.tachyon",
        "google meet" to "com.google.android.apps.tachyon",
        "zoom" to "us.zoom.videomeetings",
        "reddit" to "com.reddit.frontpage",
        "tiktok" to "com.zhiliaoapp.musically",
        "snapchat" to "com.snapchat.android",
        "linkedin" to "com.linkedin.android",
        "amazon" to "com.amazon.mShop.android.shopping",
        "flipkart" to "com.flipkart.android",
        "paytm" to "net.one97.paytm",
        "gpay" to "com.google.android.apps.nbu.paisa.user",
        "google pay" to "com.google.android.apps.nbu.paisa.user",
        "browser" to "com.android.browser"
    )

    fun launchApp(appName: String): ActionResult {
        val normalized = appName.lowercase().trim()

        // Try alias first
        val packageName = appAliases[normalized]
        if (packageName != null) {
            return launchByPackage(packageName, appName)
        }

        // Try direct package name search
        val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val match = allApps.firstOrNull { app ->
            val label = packageManager.getApplicationLabel(app).toString().lowercase()
            label == normalized || label.contains(normalized) || normalized.contains(label)
        }
        if (match != null) {
            return launchByPackage(match.packageName, appName)
        }

        return ActionResult(false, "$appName is not installed on this device.")
    }

    private fun launchByPackage(packageName: String, displayName: String): ActionResult {
        return try {
            val intent = packageManager.getLaunchIntentForPackage(packageName)
                ?: return ActionResult(false, "$displayName is not installed.")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            ActionResult(true, "Launched $displayName.")
        } catch (e: Exception) {
            ActionResult(false, "Could not launch $displayName: ${e.message}")
        }
    }

    fun openUrl(url: String): ActionResult {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Opened browser.")
        } catch (e: Exception) {
            ActionResult(false, "Could not open URL: ${e.message}")
        }
    }

    fun openSettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Opened Settings.")
        } catch (e: Exception) {
            ActionResult(false, "Could not open Settings.")
        }
    }

    fun openAccessibilitySettings(): ActionResult {
        return try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            ActionResult(true, "Opened Accessibility Settings.")
        } catch (e: Exception) {
            ActionResult(false, "Could not open Accessibility Settings.")
        }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun resolvePackageName(appName: String): String? {
        val normalized = appName.lowercase().trim()
        if (appAliases.containsKey(normalized)) return appAliases[normalized]
        val allApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        return allApps.firstOrNull { app ->
            val label = packageManager.getApplicationLabel(app).toString().lowercase()
            label == normalized || label.contains(normalized)
        }?.packageName
    }
}
