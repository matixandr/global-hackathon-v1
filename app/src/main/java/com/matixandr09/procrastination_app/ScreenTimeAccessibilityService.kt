package com.matixandr09.procrastination_app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.util.Log
import com.matixandr09.procrastination_app.screens.BlockedActivity

class ScreenTimeAccessibilityService : AccessibilityService() {

    private val blockedApps = listOf("com.google.android.youtube")
    private var previousApp: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.packageName?.let { packageName ->
            val currentApp = packageName.toString()
            if (currentApp == previousApp) {
                return // Do nothing if the app hasn't changed
            }
            previousApp = currentApp // Update the previous app

            if (currentApp in blockedApps) {
                Log.d("ScreenTimeService", "Blocking app: $currentApp")
                showBlockScreen()
            }
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    private fun showBlockScreen() {
        val intent = Intent(this, BlockedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
