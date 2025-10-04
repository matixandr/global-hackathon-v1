package com.matixandr09.procrastination_app

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class ScreenTimeAccessibilityService : AccessibilityService() {

    val blockedApps = listOf("com.google.android.youtube")

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.packageName?.let { packageName ->
            val currentApp = packageName.toString()

            if (currentApp in blockedApps) {
                Log.d("ScreenTimeService", "Blocking app: $currentApp")
            }

            // TODO: You can store this info in database for real-time tracking
        }
    }

    override fun onInterrupt() {
        // Required override
    }
}