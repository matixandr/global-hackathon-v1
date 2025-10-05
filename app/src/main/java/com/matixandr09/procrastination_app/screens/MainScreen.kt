package com.matixandr09.procrastination_app.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.matixandr09.procrastination_app.R
import com.matixandr09.procrastination_app.data.AppViewModel
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isDone: Boolean = false,
    val color: String = "FFC0CB"
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController, appViewModel: AppViewModel) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    var isListening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("...") }
    var editingTask by remember { mutableStateOf<Task?>(null) }

    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            isListening = true
            status = "Listening..."
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {
            isListening = false
            status = "..."
        }

        override fun onError(error: Int) {
            isListening = false
            status = "Error: $error"
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (matches != null && matches.isNotEmpty()) {
                text = matches[0]
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    speechRecognizer.setRecognitionListener(recognitionListener)

    val startVoiceRecognition = {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        speechRecognizer.startListening(intent)
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            status = "Permission denied"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.account),
                contentDescription = "Account",
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { navController.navigate("accounts") }
            )
            Text(text = "00:00")
            Image(
                painter = painterResource(id = R.drawable.streak),
                contentDescription = "Streak",
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { navController.navigate("streak") }
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(appViewModel.tasks) { task ->
                TaskItem(task = task, appViewModel = appViewModel, onEdit = { editingTask = it })
            }
        }

        if (editingTask != null) {
            EditTaskOverlay(
                task = editingTask!!,
                appViewModel = appViewModel,
                onDismiss = { editingTask = null })
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(380f / 440f)
                .height(60.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFF4C4C4C),
                    shape = RoundedCornerShape(10.dp)
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.addtaskbutton),
                    contentDescription = "Add Task",
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            if (text.isNotBlank()) {
                                appViewModel.addTask(text)
                                text = ""
                            }
                        }
                        .padding(start = 16.dp)
                        .size(32.dp)
                )
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = {
                        Text(
                            text = "ENTER YOUR TASK",
                            color = Color.Gray,
                            fontSize = 14.sp,
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp, end = 16.dp)
                )
                Image(
                    painter = painterResource(id = R.drawable.voicething),
                    contentDescription = "Voice Input",
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isListening
                        ) {
                            when (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            )) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    startVoiceRecognition()
                                }
                                else -> {
                                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                        .padding(end = 16.dp)
                        .size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = status,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(horizontal = 24.dp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun TaskItem(task: Task, appViewModel: AppViewModel, onEdit: (Task) -> Unit) {
    val color = Color(android.graphics.Color.parseColor("#${task.color}"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 16.dp)
            .background(color, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = if (task.isDone) R.drawable.todo_checkmark_done else R.drawable.todo_checkmark_not_done),
            contentDescription = "Checkbox",
            modifier = Modifier
                .size(24.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { appViewModel.toggleTaskDone(task) }
        )
        Text(
            text = task.text, modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.olowek),
            contentDescription = "Edit",
            modifier = Modifier
                .size(24.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onEdit(task) }
        )
        Spacer(modifier = Modifier.size(16.dp))
        Image(
            painter = painterResource(id = R.drawable.delete),
            contentDescription = "Delete",
            modifier = Modifier
                .size(24.dp)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { appViewModel.deleteTask(task) }
        )
    }
}

@Composable
fun EditTaskOverlay(task: Task, appViewModel: AppViewModel, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(task.text) }
    var selectedColor by remember { mutableStateOf(task.color) }
    val colors = listOf("CDB4DB", "FFC8DD", "FFAFCC", "BDE0FE", "A2D2FF")

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Edit Task", fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Task") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(android.graphics.Color.parseColor("#$color")), CircleShape)
                                .clickable { selectedColor = color }
                                .border(
                                    width = if (selectedColor == color) 2.dp else 0.dp,
                                    color = Color.Black,
                                    shape = CircleShape
                                )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    appViewModel.editTask(task.id, text, selectedColor)
                    onDismiss()
                }) {
                    Text("Save")
                }
            }
        }
    }
}

fun Context.openAccessibilitySettings() {
    val intent = Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
    startActivity(intent)
}
