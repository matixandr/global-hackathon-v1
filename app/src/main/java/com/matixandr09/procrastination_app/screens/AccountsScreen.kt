package com.matixandr09.procrastination_app.screens

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import com.matixandr09.procrastination_app.R
import com.matixandr09.procrastination_app.ScreenTimeAccessibilityService
import com.matixandr09.procrastination_app.services.SessionVerificationService
import io.ktor.client.* 
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.* 
import io.ktor.client.request.* 
import io.ktor.client.statement.*
import io.ktor.http.* 
import io.ktor.serialization.kotlinx.json.* 
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class RegisterRequest(val email: String, val password: String, val username: String)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class User(val user_metadata: UserMetadata)

@Serializable
data class UserMetadata(val username: String)

@Serializable
data class UserSession(val access_token: String, val token_type: String, val expires_in: Long, val refresh_token: String)

@Serializable
data class LoginResponse(val user: User, val session: UserSession)

@Composable
fun AccountsScreen(navController: NavController) {
    val context = LocalContext.current
    val sharedPrefs = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
    var isServiceEnabled by remember { mutableStateOf(isAccessibilityServiceEnabled(context)) }
    var timeLimit by remember { mutableStateOf(sharedPrefs.getInt("time_limit_minutes", 30).toString()) }
    var showAuthOverlay by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    var loggedInUsername by remember { mutableStateOf<String?>(null) }
    var showLoggedOutPopup by remember { mutableStateOf(false) }

    val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loggedInUsername = null
            showLoggedOutPopup = true
        }
    }

    DisposableEffect(Unit) {
        val filter = IntentFilter("com.matixandr09.procrastination_app.LOGOUT")
        context.registerReceiver(logoutReceiver, filter)
        onDispose {
            context.unregisterReceiver(logoutReceiver)
        }
    }

    LaunchedEffect(Unit) {
        val savedToken = sharedPrefs.getString("user_token", null)
        val savedUsername = sharedPrefs.getString("username", null)
        if (savedToken != null && savedUsername != null) {
            loggedInUsername = savedUsername
            context.startService(Intent(context, SessionVerificationService::class.java))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            // Top banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFA2D2FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 16.dp)
                        .size(32.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            navController.popBackStack()
                        }
                )
                Text(
                    text = "Account",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 40.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // The grey box
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0F0F0))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.account),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                    )
                    Column(modifier = Modifier.padding(start = 16.dp)) {
                        if (loggedInUsername != null) {
                            Text("Logged in as: $loggedInUsername", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text("Not logged in", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (loggedInUsername == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    // Buttons under the box
                    Row {
                        Button(onClick = {
                            showAuthOverlay = true
                            isLogin = true
                        }) {
                            Text("Log In")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            showAuthOverlay = true
                            isLogin = false
                        }) {
                            Text("Register")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF0F0F0))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Enable App Blocking", fontWeight = FontWeight.Medium)
                        Switch(
                            checked = isServiceEnabled,
                            onCheckedChange = {
                                openAccessibilitySettings(context)
                                isServiceEnabled = it
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFA2D2FF),
                                checkedTrackColor = Color(0xFFA2D2FF).copy(alpha = 0.5f)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Screen Time Limit", fontWeight = FontWeight.Medium)
                        TextField(
                            value = timeLimit,
                            onValueChange = { newTime ->
                                timeLimit = newTime
                                newTime.toIntOrNull()?.let {
                                    sharedPrefs.edit().putInt("time_limit_minutes", it).apply()
                                }
                            },
                            modifier = Modifier.width(80.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate("app_selection") },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA2D2FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Block Social Media Apps", color = Color.White)
                }
            }
        }

        if (showLoggedOutPopup) {
            AlertDialog(
                onDismissRequest = { showLoggedOutPopup = false },
                title = { Text(text = "Logged Out") },
                text = { Text(text = "Your session has expired. Please log in again.") },
                confirmButton = {
                    Button(onClick = { showLoggedOutPopup = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showAuthOverlay) {
            AuthOverlay(isLogin = isLogin, onDismiss = { showAuthOverlay = false }) { email, password, username ->
                coroutineScope.launch {
                    val client = HttpClient(Android) {
                        install(ContentNegotiation) {
                            json(Json { ignoreUnknownKeys = true })
                        }
                    }
                    if (isLogin) {
                        val request = LoginRequest(email, password)
                        try {
                            val response: HttpResponse = client.post("https://obszdnckcefuszlilzsd.supabase.co/functions/v1/login") {
                                contentType(ContentType.Application.Json)
                                setBody(request)
                            }
                            if (response.status == HttpStatusCode.OK) {
                                val loginResponse = response.body<LoginResponse>()
                                sharedPrefs.edit {
                                    putString("user_token", loginResponse.session.access_token)
                                    putString("username", loginResponse.user.user_metadata.username)
                                    apply()
                                }
                                loggedInUsername = loginResponse.user.user_metadata.username
                                showAuthOverlay = false
                                context.startService(Intent(context, SessionVerificationService::class.java))
                            } else {
                                Log.e("Auth", "Login failed: ${response.bodyAsText()}")
                            }
                        } catch (e: Exception) {
                            Log.e("Auth", "Error: ${e.message}")
                        } finally {
                            client.close()
                        }
                    } else {
                        val request = RegisterRequest(email, password, username)
                        try {
                            val response: HttpResponse = client.post("https://obszdnckcefuszlilzsd.supabase.co/functions/v1/register") {
                                contentType(ContentType.Application.Json)
                                setBody(request)
                            }
                            if (response.status == HttpStatusCode.OK) {
                                val session = response.body<UserSession>()
                                sharedPrefs.edit {
                                    putString("user_token", session.access_token)
                                    putString("username", username)
                                    apply()
                                }
                                loggedInUsername = username
                                showAuthOverlay = false
                                context.startService(Intent(context, SessionVerificationService::class.java))
                            } else {
                                Log.e("Auth", "Registration failed: ${response.bodyAsText()}")
                            }
                        } catch (e: Exception) {
                            Log.e("Auth", "Error: ${e.message}")
                        } finally {
                            client.close()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthOverlay(isLogin: Boolean, onDismiss: () -> Unit, onAuth: (String, String, String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Consume clicks to prevent background click
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (isLogin) "Log In" else "Register", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            if (!isLogin) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") })
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, "toggle password visibility")
                    }
                }
            )
            if (!isLogin) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = { Text("Repeat Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, "toggle password visibility")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                if(isLogin) {
                    onAuth(email, password, "")
                } else {
                    if (password == repeatPassword) {
                        onAuth(email, password, username)
                    } else {
                        Log.d("Auth", "Passwords do not match")
                    }
                }
            }) {
                Text(if (isLogin) "Log In" else "Register")
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val service = "${context.packageName}/${ScreenTimeAccessibilityService::class.java.canonicalName}"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(service) == true
}

private fun openAccessibilitySettings(context: Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}
