// app/src/main/java/com/iie/st10089153/txdevsystems_app/security/SessionManager.kt
package com.iie.st10089153.txdevsystems_app.security

import android.content.Context
import kotlinx.coroutines.*

class SessionManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private var sessionJob: Job? = null
    private var lastActivityTime = System.currentTimeMillis()
    private var isSessionActive = false

    private val tokenManager = SecureTokenManager.getInstance(context)

    fun startSession() {
        isSessionActive = true
        lastActivityTime = System.currentTimeMillis()
        startSessionTimer()
    }

    fun refreshSession() {
        lastActivityTime = System.currentTimeMillis()
    }

    fun endSession() {
        isSessionActive = false
        sessionJob?.cancel()
        tokenManager.clearAuthToken()
    }

    private fun startSessionTimer() {
        sessionJob?.cancel()
        sessionJob = CoroutineScope(Dispatchers.Main).launch {
            while (isSessionActive) {
                delay(60000) // Check every minute

                val currentTime = System.currentTimeMillis()
                val timeSinceLastActivity = currentTime - lastActivityTime

                if (timeSinceLastActivity > SecurityConfig.SESSION_TIMEOUT_MINUTES * 60 * 1000) {
                    // Session expired
                    endSession()
                    // Navigate to login screen
                    onSessionExpired()
                    break
                }
            }
        }
    }

    private fun onSessionExpired() {
        // Implement navigation to login screen
        // You can use a callback or event bus for this
    }

    fun isSessionValid(): Boolean {
        return isSessionActive && tokenManager.isTokenValid()
    }
}