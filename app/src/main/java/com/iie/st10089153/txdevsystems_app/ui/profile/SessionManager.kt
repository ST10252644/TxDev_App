package com.iie.st10089153.txdevsystems_app.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    // Save session data (optional, used during login)
    fun saveLoginSession(
        accessToken: String,
        refreshToken: String?,
        userId: String?,
        userEmail: String?,
        rememberMe: Boolean
    ) {
        val editor = prefs.edit()
        editor.putString("access_token", accessToken)
        editor.putString("refresh_token", refreshToken)
        editor.putString("user_id", userId)
        editor.putString("user_email", userEmail)
        editor.putBoolean("remember_me", rememberMe)
        editor.putBoolean("is_logged_in", true)
        editor.apply()
    }

    // Check if user is logged in
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("is_logged_in", false)
    }

    // Check if "Remember Me" is enabled
    fun isRememberMeEnabled(): Boolean {
        return prefs.getBoolean("remember_me", false)
    }

    // Clear the entire session (used for logout)
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
