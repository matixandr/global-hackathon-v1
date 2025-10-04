package com.matixandr09.procrastination_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.matixandr09.procrastination_app.openAccessibilitySettings

@Composable
fun MainScreen(navController: NavController) {
    // Get the current Context
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { navController.navigate("accounts") }) {
            Text("Accounts")
        }

        Button(onClick = { context.openAccessibilitySettings() }) {
            Text("Enable Accessibility")
        }

        Button(onClick = { navController.navigate("streak") }) {
            Text("Streak")
        }
    }
}
