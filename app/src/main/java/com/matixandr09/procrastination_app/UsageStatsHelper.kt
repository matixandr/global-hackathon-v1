package com.matixandr09.procrastination_app

import android.app.usage.UsageStatsManager
import android.content.Context

fun Context.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
    val mode = appOps.checkOpNoThrow(
        android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
        android.os.Process.myUid(),
        packageName
    )
    return mode == android.app.AppOpsManager.MODE_ALLOWED
}

fun Context.requestUsageStatsPermission() {
    startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
}

fun Context.getAppUsageStats(): Map<String, Long> {
    val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    val endTime = System.currentTimeMillis()
    val startTime = endTime - 24 * 60 * 60 * 1000 // last 24 hours

    val usageStatsList = usageStatsManager.queryUsageStats(
        UsageStatsManager.INTERVAL_DAILY, startTime, endTime
    )

    val usageMap = mutableMapOf<String, Long>()
    usageStatsList?.forEach { stats ->
        usageMap[stats.packageName] = stats.totalTimeInForeground
    }
    return usageMap
}
