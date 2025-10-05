package com.matixandr09.procrastination_app.screens

import android.content.Context
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@Composable
fun AppSelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val packageManager = context.packageManager
    var apps by remember { mutableStateOf<List<ApplicationInfo>>(emptyList()) }
    val sharedPrefs = context.getSharedPreferences("blocked_apps", Context.MODE_PRIVATE)
    var blockedApps by remember { mutableStateOf(sharedPrefs.getStringSet("blocked_apps", emptySet()) ?: emptySet()) }

    LaunchedEffect(Unit) {
        apps = packageManager.getInstalledApplications(0)
            .filter { appInfo ->
                val isNotSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                val isLaunchable = packageManager.getLaunchIntentForPackage(appInfo.packageName) != null

                if (!isNotSystemApp || !isLaunchable) {
                    false
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        when (appInfo.category) {
                            ApplicationInfo.CATEGORY_SOCIAL,
                            ApplicationInfo.CATEGORY_VIDEO -> true
                            else -> false
                        }
                    } else {
                        // Fallback for older APIs: show all non-system, launchable apps.
                        true
                    }
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFFA2D2FF))
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .size(32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        navController.popBackStack()
                    }
            )
            Text(
                text = "App Selection",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(apps) { app ->
                val isChecked = blockedApps.contains(app.packageName)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0F0F0))
                        .clickable { 
                            val newBlockedApps = blockedApps.toMutableSet()
                            if (isChecked) {
                                newBlockedApps.remove(app.packageName)
                            } else {
                                newBlockedApps.add(app.packageName)
                            }
                            blockedApps = newBlockedApps
                            sharedPrefs.edit().putStringSet("blocked_apps", newBlockedApps).apply()
                        }
                        .padding(16.dp),
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
                            modifier = Modifier.padding(start = 16.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Checkbox(
                        checked = isChecked, 
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFFA2D2FF)
                        )
                    )
                }
            }
        }
    }
}
