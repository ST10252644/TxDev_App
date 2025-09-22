// app/src/main/java/com/iie/st10089153/txdevsystems_app/security/SecureTokenManager.kt
package com.iie.st10089153.txdevsystems_app.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class SecureTokenManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: SecureTokenManager? = null

        fun getInstance(context: Context): SecureTokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SecureTokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAuthToken(token: String) {
        encryptedPrefs.edit()
            .putString("auth_token", token)
            .putLong("token_timestamp", System.currentTimeMillis())
            .apply()
    }

    fun getAuthToken(): String? {
        val token = encryptedPrefs.getString("auth_token", null)
        val timestamp = encryptedPrefs.getLong("token_timestamp", 0)

        // Check if token is expired
        if (System.currentTimeMillis() - timestamp > SecurityConfig.TOKEN_EXPIRY_HOURS * 3600000) {
            clearAuthToken()
            return null
        }

        return token
    }

    fun clearAuthToken() {
        encryptedPrefs.edit()
            .remove("auth_token")
            .remove("token_timestamp")
            .apply()
    }

    fun isTokenValid(): Boolean {
        return getAuthToken() != null
    }
}