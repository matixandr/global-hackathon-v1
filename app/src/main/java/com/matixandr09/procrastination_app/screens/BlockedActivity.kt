package com.matixandr09.procrastination_app.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.matixandr09.procrastination_app.ui.theme.ProcrastinationappTheme

class BlockedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProcrastinationappTheme {
                Box(
                    modifier = Modifier.Companion.fillMaxSize(),
                    contentAlignment = Alignment.Companion.Center
                ) {
                    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                        Text("This app is blocked!")
                        Spacer(modifier = Modifier.Companion.height(16.dp))
                        Button(onClick = { finish() }) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}