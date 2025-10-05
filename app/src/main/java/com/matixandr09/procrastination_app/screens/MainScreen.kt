package com.matixandr09.procrastination_app.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.matixandr09.procrastination_app.R
import com.matixandr09.procrastination_app.data.AppViewModel
import java.time.LocalDate
import java.util.UUID

data class Task(val id: String = UUID.randomUUID().toString(), var text: String, var isDone: Boolean = false)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(navController: NavController, appViewModel: AppViewModel) {
    var text by remember { mutableStateOf("") }
    val tasks = remember { mutableStateListOf<Task>() }
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Naciśnij mikrofon, aby mówić.") }
    var editingTaskId by remember { mutableStateOf<String?>(null) }
    var editingTaskText by remember { mutableStateOf("") }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    val speechRecognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "pl-PL")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
    }

    DisposableEffect(key1 = speechRecognizer) {
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                status = "Słucham..."
            }

            override fun onBeginningOfSpeech() {
                status = "Wykryto mowę..."
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                status = "Przetwarzanie zakończone."
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Błąd nagrywania dźwięku."
                    SpeechRecognizer.ERROR_CLIENT -> "Błąd po stronie klienta."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Brak uprawnień do mikrofonu."
                    SpeechRecognizer.ERROR_NETWORK -> "Błąd sieci."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Przekroczono czas oczekiwania sieci."
                    SpeechRecognizer.ERROR_NO_MATCH -> "Nie rozpoznano mowy."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Usługa rozpoznawania jest zajęta."
                    SpeechRecognizer.ERROR_SERVER -> "Błąd serwera."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Nie wykryto mowy."
                    else -> "Nieznany błąd rozpoznawania mowy."
                }
                status = errorMessage
                isListening = false
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    text = matches[0]
                }
                status = "Gotowe! Naciśnij, by mówić."
                isListening = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    text = matches[0]
                    status = "Rozpoznawanie na żywo..."
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
        speechRecognizer.setRecognitionListener(recognitionListener)

        onDispose {
            speechRecognizer.destroy()
        }
    }

    val startVoiceRecognition = {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            status = "Na tym urządzeniu nie znaleziono usługi rozpoznawania mowy."
        } else if (!isListening) {
            status = "Inicjalizacja..."
            speechRecognizer.startListening(speechRecognizerIntent)
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startVoiceRecognition()
        } else {
            status = "Odmówiono uprawnień do mikrofonu."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
    ) {
        // Top navigation bar
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
            Text(text = "00:00") // This will be dynamic in the future
            Image(
                painter = painterResource(id = R.drawable.streak),
                contentDescription = "Streak",
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { navController.navigate("streak") }
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(tasks, key = { it.id }) { task ->
                Box(
                    modifier = Modifier
                        .animateItemPlacement(
                            animationSpec = tween(durationMillis = 500)
                        )
                        .fillMaxWidth(330f / 440f)
                        .padding(vertical = 4.dp)
                        .background(Color(0xFFFFC8DD), shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = if (task.isDone) R.drawable.todo_checkmark_done else R.drawable.todo_checkmark_not_done),
                            contentDescription = "Checkmark",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    val taskIndex = tasks.indexOf(task)
                                    if (taskIndex != -1) {
                                        val updatedTask = tasks[taskIndex].copy(isDone = !tasks[taskIndex].isDone)
                                        tasks[taskIndex] = updatedTask
                                        if (updatedTask.isDone) {
                                            appViewModel.addCompletedDate(LocalDate.now())
                                        } else {
                                            appViewModel.removeCompletedDate(LocalDate.now())
                                        }
                                    }
                                }
                        )
                        if (editingTaskId == task.id) {
                            TextField(
                                value = editingTaskText,
                                onValueChange = { editingTaskText = it },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp),
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
                            )
                        } else {
                            Text(
                                text = task.text,
                                color = Color.Black,
                                fontSize = 16.sp,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 16.dp)
                            )
                        }
                        if (editingTaskId == task.id) {
                            Image(
                                painter = painterResource(id = R.drawable.olowek),
                                contentDescription = "Save",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        val taskIndex = tasks.indexOfFirst { it.id == editingTaskId }
                                        if (taskIndex != -1) {
                                            tasks[taskIndex] = tasks[taskIndex].copy(text = editingTaskText)
                                        }
                                        editingTaskId = null
                                    }
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.olowek),
                                contentDescription = "Edit",
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable {
                                        editingTaskId = task.id
                                        editingTaskText = task.text
                                    }
                            )
                        }
                        Spacer(modifier = Modifier.size(16.dp))
                        Image(
                            painter = painterResource(id = R.drawable.delete),
                            contentDescription = "Delete",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    tasks.remove(task)
                                }
                        )
                    }
                }
            }
        }

        // Bottom centered box
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(380f / 440f) // ~86% of screen width
                .height(60.dp) // Height
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
                                tasks.add(0, Task(text = text))
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
