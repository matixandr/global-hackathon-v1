package com.matixandr09.procrastination_app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.FrameLayout

class ScreenTimeAccessibilityService : AccessibilityService() {

    private val blockedApps = listOf("com.google.android.youtube")
    private var overlayView: FrameLayout? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.packageName?.let { packageName ->
            val currentApp = packageName.toString()
            if (currentApp in blockedApps) {
                Log.d("ScreenTimeService", "Blocking app: $currentApp")
                showOverlay()
            } else {
                removeOverlay()
            }
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    private fun showOverlay() {
        if (overlayView != null) return // Already showing

        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.overlay_blocked, null) as FrameLayout

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT
        )

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        wm.addView(overlayView, params)
    }

    private fun removeOverlay() {
        overlayView?.let {
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            wm.removeView(it)
            overlayView = null
        }
    }
}
