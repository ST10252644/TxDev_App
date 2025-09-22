package com.iie.st10089153.txdevsystems_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.iie.st10089153.txdevsystems_app.MainActivity
import com.iie.st10089153.txdevsystems_app.R
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePasswordButton: ImageButton
    private var isPasswordVisible = false

    // Minimal security additions
    private var loginAttempts = 0
    private val maxLoginAttempts = 5 // Increased to be less restrictive

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views (same as original)
        loginButton = findViewById(R.id.loginButton)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        togglePasswordButton = findViewById(R.id.btnTogglePassword)

        // Keep your original security flag
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Set up click listeners (same as original)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            // Minimal validation (less restrictive than before)
            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }

        // Keep your original password toggle functionality
        togglePasswordButton.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordButton.setImageResource(R.drawable.ic_visibility)
            } else {
                passwordInput.inputType =
                    android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordButton.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordInput.setSelection(passwordInput.text.length)
        }
    }

    private fun performLogin(username: String, password: String) {
        // Check login attempts (only minimal protection)
        if (loginAttempts >= maxLoginAttempts) {
            Toast.makeText(this, "Please wait before trying again", Toast.LENGTH_LONG).show()
            return
        }

        // Your original API call - completely unchanged
        val call = RetrofitClient.getAuthApi(this).login(username = username, password = password)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.access_token

                    // Enhanced token saving (but keeps your original method too)
                    saveToken(token) // Your original method
                    saveTokenWithTimestamp(token) // Additional security copy

                    // Reset attempts on success
                    loginAttempts = 0

                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Navigate to MainActivity (exactly as original)
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Minimal failure handling
                    loginAttempts++
                    val remaining = maxLoginAttempts - loginAttempts
                    if (remaining > 0) {
                        Toast.makeText(this@LoginActivity, "Login failed: Invalid credentials ($remaining attempts left)", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login failed: Please try again later", Toast.LENGTH_LONG).show()
                    }
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Login failed: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    // Your original token saving method - kept exactly as is
    private fun saveToken(token: String) {
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        sharedPref.edit()
            .putString("access_token", token)
            .apply()
    }

    // Additional token saving with timestamp (doesn't interfere with original)
    private fun saveTokenWithTimestamp(token: String) {
        val sharedPref = getSharedPreferences("secure_auth", MODE_PRIVATE)
        sharedPref.edit()
            .putString("backup_token", token)
            .putLong("login_time", System.currentTimeMillis())
            .apply()
    }
}