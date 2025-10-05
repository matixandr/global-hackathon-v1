package com.matixandr09.procrastination_app.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.content.edit
import com.matixandr09.procrastination_app.screens.LoginResponse
import io.ktor.client.* 
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.* 
import io.ktor.client.request.* 
import io.ktor.client.statement.*
import io.ktor.http.* 
import io.ktor.serialization.kotlinx.json.* 
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class VerifySessionRequest(val access_token: String)

@Serializable
data class VerifySessionResponse(val valid: Boolean, val user: LoginResponse? = null, val detail: String? = null)

class SessionVerificationService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val verificationInterval = 10 * 60 * 1000L // 10 minutes
    private val verificationRunnable = object : Runnable {
        override fun run() {
            verifySession()
            handler.postDelayed(this, verificationInterval)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("SessionService", "Service started")
        handler.post(verificationRunnable)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("SessionService", "Service stopped")
        handler.removeCallbacks(verificationRunnable)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun verifySession() {
        val sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        val token = sharedPrefs.getString("user_token", null)

        if (token == null) {
            Log.d("SessionService", "No token found, stopping service")
            stopSelf()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val client = HttpClient(Android) {
                install(ContentNegotiation) {
                    json(Json { ignoreUnknownKeys = true })
                }
            }
            try {
                val response: HttpResponse = client.post("https://obszdnckcefuszlilzsd.supabase.co/functions/v1/verify-session") {
                    contentType(ContentType.Application.Json)
                    setBody(VerifySessionRequest(token))
                }

                if (response.status == HttpStatusCode.OK) {
                    val verificationResponse = response.body<VerifySessionResponse>()
                    if (!verificationResponse.valid) {
                        Log.d("SessionService", "Session is invalid, logging out")
                        logout()
                    }
                } else {
                    Log.e("SessionService", "Verification failed: ${response.bodyAsText()}")
                    logout()
                }
            } catch (e: Exception) {
                Log.e("SessionService", "Error during session verification: ${e.message}")
            } finally {
                client.close()
            }
        }
    }

    private fun logout() {
        val sharedPrefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        sharedPrefs.edit {
            remove("user_token")
            remove("username")
            apply()
        }
        val intent = Intent("com.matixandr09.procrastination_app.LOGOUT")
        sendBroadcast(intent)
        stopSelf()
    }
}