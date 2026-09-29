package dev.shlok.samai.androidcontrol

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SamAccessibilityService : AccessibilityService() {

    companion object {
        private val _instance = MutableStateFlow<SamAccessibilityService?>(null)
        val instance: StateFlow<SamAccessibilityService?> = _instance

        fun isConnected() = _instance.value != null
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        _instance.value = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        _instance.value = null
    }

    fun performGlobalBack(): Boolean = performGlobalAction(GLOBAL_ACTION_BACK)

    fun performGlobalHome(): Boolean = performGlobalAction(GLOBAL_ACTION_HOME)

    fun performGlobalRecents(): Boolean = performGlobalAction(GLOBAL_ACTION_RECENTS)

    fun performGlobalNotifications(): Boolean = performGlobalAction(GLOBAL_ACTION_NOTIFICATIONS)

    fun scrollDown(): Boolean {
        val displayMetrics = resources.displayMetrics
        val w = displayMetrics.widthPixels
        val h = displayMetrics.heightPixels
        return performSwipe(w / 2f, h * 0.7f, w / 2f, h * 0.3f)
    }

    fun scrollUp(): Boolean {
        val displayMetrics = resources.displayMetrics
        val w = displayMetrics.widthPixels
        val h = displayMetrics.heightPixels
        return performSwipe(w / 2f, h * 0.3f, w / 2f, h * 0.7f)
    }

    private fun performSwipe(x1: Float, y1: Float, x2: Float, y2: Float): Boolean {
        val path = Path().apply {
            moveTo(x1, y1)
            lineTo(x2, y2)
        }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 300))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun tapAt(x: Float, y: Float): Boolean {
        val path = Path().apply { moveTo(x, y) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, 50))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    fun findAndClickNodeWithText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val nodes = rootNode.findAccessibilityNodeInfosByText(text)
        if (nodes.isNullOrEmpty()) return false
        val node = nodes.first()
        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }
        // Try clicking parent
        var parent = node.parent
        var attempts = 0
        while (parent != null && attempts < 5) {
            if (parent.isClickable) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            parent = parent.parent
            attempts++
        }
        return false
    }

    fun typeText(text: String): Boolean {
        val rootNode = rootInActiveWindow ?: return false
        val focusedNode = rootNode.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focusedNode != null) {
            val args = android.os.Bundle()
            args.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
            focusedNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
            return true
        }
        return false
    }

    fun getVisibleText(): String {
        val root = rootInActiveWindow ?: return ""
        return extractAllText(root)
    }

    private fun extractAllText(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        if (!node.text.isNullOrBlank()) sb.append(node.text).append(" ")
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            sb.append(extractAllText(child))
        }
        return sb.toString()
    }
}
