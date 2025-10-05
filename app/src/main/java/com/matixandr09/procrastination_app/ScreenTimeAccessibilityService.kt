package com.matixandr09.procrastination_app

import android.accessibilityservice.AccessibilityService
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.matixandr09.procrastination_app.data.socialMediaApps
import com.matixandr09.procrastination_app.screens.BlockedActivity
import java.util.Locale

// AccessibilityService API usage must align with the API policy guidelines.
// It must not be used to bypass privacy controls or change settings without consent.
class ScreenTimeAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val timeLimitMinutes = sharedPrefs.getInt("time_limit_minutes", 30)
        val timeLimitMs = timeLimitMinutes * 60 * 1000

        val blockedAppsPrefs = getSharedPreferences("blocked_apps", MODE_PRIVATE)
        val blockedApps = blockedAppsPrefs.getStringSet("blocked_apps", socialMediaApps) ?: socialMediaApps

        event?.packageName?.let { packageName ->
            val currentApp = packageName.toString()

            if (currentApp in blockedApps) {
                Log.d("ScreenTimeService", "Blocking app: $currentApp")

                if (!hasUsageStatsPermission()) {
                    Log.d("ScreenTimeService", "Usage access not granted!")
                    requestUsageStatsPermission()
                    return
                }

                val usageMs = getAppUsageTime(currentApp)
                val formatted = usageMs.formatTime()
                Log.d("ScreenTimeService", "User has used $currentApp for $formatted today")

                if (usageMs >= timeLimitMs) {
                    showBlockScreen()
                } else {
                    Log.d("ScreenTimeService", "Not blocking app: $currentApp")
                }
            }
        }
    }

    override fun onInterrupt() {
        // Required override
    }

    /**
     * Returns total usage time in milliseconds for a given package over the last 24 hours
     */
    private fun getAppUsageTime(packageName: String): Long {
        val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 24 * 60 * 60 * 1000

        // Use queryAndAggregateUsageStats for accurate totals
        val usageMap = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
        return usageMap[packageName]?.totalTimeInForeground ?: 0
    }

    /**
     * Converts milliseconds to HH:mm:ss format
     */
    private fun Long.formatTime(): String {
        val hours = this / 1000 / 3600
        val minutes = (this / 1000 / 60) % 60
        val seconds = (this / 1000) % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Launches the block screen activity
     */
    private fun showBlockScreen() {
        val intent = Intent(this, BlockedActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    /**
     * Checks if the app has Usage Access permission
     */
    private fun hasUsageStatsPermission(): Boolean {
        val appOps = getSystemService(APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )
        return mode == android.app.AppOpsManager.MODE_ALLOWED
    }

    /**
     * Opens Usage Access Settings to request permission
     */
    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}
