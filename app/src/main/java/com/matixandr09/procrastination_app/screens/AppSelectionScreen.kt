package com.matixandr09.procrastination_app.screens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

val socialMediaApps = setOf(
    "com.instagram.android",
    "com.facebook.katana",
    "com.twitter.android",
    "com.snapchat.android",
    "com.tiktok.musical.ly",
    "com.pinterest",
    "com.linkedin.android",
    "com.reddit.frontpage",
    "com.whatsapp",
    "com.facebook.orca",
    "com.google.android.youtube"
)

@Composable
fun AppSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
    val sharedPrefs = context.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE)
    var blockedApps by remember { mutableStateOf(sharedPrefs.getStringSet("blocked_apps", socialMediaApps) ?: socialMediaApps) }

    LaunchedEffect(Unit) {
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(mainIntent, 0)
            .map { it.activityInfo.applicationInfo }
            .filter { it.packageName in socialMediaApps }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "Select Social Media Apps to Block")
        LazyColumn {
            items(apps) { app ->
                val isChecked = blockedApps.contains(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { 
                            val newBlockedApps = blockedApps.toMutableSet()
                            if (isChecked) {
                                newBlockedApps.remove(app.packageName)
                            } else {
                                newBlockedApps.add(app.packageName)
                            }
                            blockedApps = newBlockedApps
                            sharedPrefs.edit().putStringSet("blocked_apps", newBlockedApps).apply()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = rememberAsyncImagePainter(model = app.loadIcon(packageManager)),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = app.loadLabel(packageManager).toString(),
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                    Checkbox(checked = isChecked, onCheckedChange = null)
                }
            }
        }
    }
}
