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
    "com.google.android.youtube",          // YouTube
    "com.instagram.android",               // Instagram
    "com.snapchat.android",                // Snapchat
    "com.tiktok.android",                  // TikTok
    "com.facebook.orca",                   // Facebook Messenger
    "com.whatsapp",                        // WhatsApp
    "com.facebook.katana",                 // Facebook
    "com.twitter.android",                 // Twitter / X
    "com.reddit.frontpage",                // Reddit
    "com.pinterest",                       // Pinterest
    "com.tumblr",                          // Tumblr
    "com.vk.android",                      // VK (Russia)
    "com.kakao.talk",                       // KakaoTalk (South Korea)
    "jp.naver.line.android",               // LINE (Japan / Asia)
    "com.zhiliaoapp.musically",            // TikTok older package
    "com.clubhouse.app",                   // Clubhouse
    "com.discord",                         // Discord

    // Messaging & Social Networks
    "com.skype.raider",                    // Skype
    "com.telegram.messenger",              // Telegram
    "com.signal.android",                  // Signal
    "com.wechat",                          // WeChat
    "com.tencent.mm",                      // Tencent WeChat
    "com.kik",                             // Kik
    "com.hike.chat",                       // Hike Messenger
    "com.linecorp.linelite",               // LINE Lite
    "com.linkedin.android",                // LinkedIn
    "com.nextdoor",                        // Nextdoor

    // Regional or niche social media
    "com.meesho.app",                      // Meesho
    "in.startv.hotstar",                   // Hotstar (video social)
    "com.badoo.mobile",                     // Badoo
    "com.tinder",                          // Tinder (dating/social)
    "com.okcupid.okcupid",                  // OkCupid
    "com.happn.app",                        // Happn
    "com.douyin",                           // Douyin (China TikTok)
    "com.kwai.video",                       // Kwai (Asia)
    "com.likee.video",                      // Likee
    "com.bytedance.lark",                    // Lark (social collaboration)
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
