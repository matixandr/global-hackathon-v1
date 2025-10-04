package com.matixandr09.procrastination_app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class ScreenTimeAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.packageName?.let { packageName ->
            val currentApp = packageName.toString()
            Log.d("ScreenTimeService", "Current app: $currentApp")

            // TODO: You can store this info in database for real-time tracking
        }
    }

    override fun onInterrupt() {
        // Required override
    }
}
