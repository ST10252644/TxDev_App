package com.iie.st10089153.txdevsystems_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.iie.st10089153.txdevsystems_app.MainActivity
import com.iie.st10089153.txdevsystems_app.R
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import android.widget.ProgressBar
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    // UI Components
    private lateinit var loginButton: Button
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePasswordButton: ImageButton
    private var progressBar: ProgressBar? = null
    private var isPasswordVisible = false

    // Performance & Security Variables
    private var loginAttempts = 0
    private val maxLoginAttempts = 5
    private var isLoggingIn = false
    private var lastFailedAttemptTime = 0L
    private val lockoutDurationMs = 30000L // 30 seconds
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        setupClickListeners()
        setupKeyboardHandling()
        checkExistingSession()
    }

    private fun initializeViews() {
        // Initialize core views
        loginButton = findViewById(R.id.loginButton)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        togglePasswordButton = findViewById(R.id.btnTogglePassword)

        // Optional progress bar - graceful handling
        progressBar = try {
            findViewById<ProgressBar>(R.id.progressBar).apply {
                visibility = View.GONE
            }
        } catch (e: Exception) {
            null // Gracefully handle missing progress bar
        }

        // Apply security settings
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        // Set initial UI state
        setInitialUIState()
    }

    private fun setInitialUIState() {
        loginButton.text = "Login"
        usernameInput.hint = "Username"
        passwordInput.hint = "Password"

        // Focus on username field for better UX
        usernameInput.requestFocus()
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            if (!isLoggingIn) {
                hideKeyboard()
                performLoginValidation()
            }
        }

        togglePasswordButton.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun setupKeyboardHandling() {
        // Allow Enter key to submit from password field
        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_GO -> {
                    loginButton.performClick()
                    true
                }
                else -> false
            }
        }

        // Auto-focus password when username is complete
        usernameInput.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_NEXT -> {
                    passwordInput.requestFocus()
                    true
                }
                else -> false
            }
        }
    }

    private fun checkExistingSession() {
        lifecycleScope.launch {
            try {
                if (hasValidSession()) {
                    showSessionRestoredMessage()
                    delay(500) // Brief delay for smooth UX
                    navigateToMainActivity()
                }
            } catch (e: Exception) {
                // Continue with normal login flow
            }
        }
    }

    private fun performLoginValidation() {
        val username = usernameInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()

        // Clear previous errors
        clearErrors()

        // Validate input efficiently
        when {
            isInLockoutPeriod() -> {
                showLockoutMessage()
                return
            }
            username.isBlank() -> {
                showFieldError(usernameInput, "Username is required")
                return
            }
            password.isBlank() -> {
                showFieldError(passwordInput, "Password is required")
                return
            }
            loginAttempts >= maxLoginAttempts -> {
                showMaxAttemptsMessage()
                return
            }
        }

        performLogin(username, password)
    }

    private fun performLogin(username: String, password: String) {
        showLoading(true)
        isLoggingIn = true

        val call = RetrofitClient.getAuthApi(this).login(username = username, password = password)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                handleLoginResponse(response)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                handleLoginFailure(t.localizedMessage ?: "Network connection failed")
            }
        })
    }

    private fun handleLoginResponse(response: Response<LoginResponse>) {
        showLoading(false)
        isLoggingIn = false

        if (response.isSuccessful && response.body() != null) {
            val token = response.body()!!.access_token
            handleLoginSuccess(token)
        } else {
            val errorMessage = when (response.code()) {
                401 -> "Invalid username or password"
                403 -> "Account access denied"
                429 -> "Too many requests. Please wait."
                else -> "Login failed. Please try again."
            }
            handleLoginFailure(errorMessage)
        }
    }

    private fun handleLoginSuccess(token: String) {
        // Save tokens efficiently
        saveTokensSecurely(token)

        // Reset security counters
        resetSecurityState()

        // Show success message
        showSuccessMessage()

        // Navigate with smooth transition
        lifecycleScope.launch {
            delay(800) // Allow user to see success message
            navigateToMainActivity()
        }
    }

    private fun handleLoginFailure(message: String) {
        showLoading(false)
        isLoggingIn = false

        loginAttempts++
        lastFailedAttemptTime = System.currentTimeMillis()

        // Security: Clear password field
        passwordInput.text.clear()

        // Show appropriate error message
        showFailureMessage(message)

        // Focus on username for retry
        handler.postDelayed({
            usernameInput.requestFocus()
        }, 100)
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val cursorPosition = passwordInput.selectionStart

        if (isPasswordVisible) {
            passwordInput.inputType = android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            togglePasswordButton.setImageResource(R.drawable.ic_visibility)
        } else {
            passwordInput.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            togglePasswordButton.setImageResource(R.drawable.ic_visibility_off)
        }

        // Maintain cursor position for smooth UX
        passwordInput.setSelection(minOf(cursorPosition, passwordInput.text.length))
    }

    private fun showLoading(show: Boolean) {
        progressBar?.visibility = if (show) View.VISIBLE else View.GONE

        // Update button state
        loginButton.apply {
            isEnabled = !show
            text = if (show) "Signing In..." else "Login"
        }

        // Disable inputs during loading
        usernameInput.isEnabled = !show
        passwordInput.isEnabled = !show
        togglePasswordButton.isEnabled = !show
    }

    private fun navigateToMainActivity() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        } catch (e: Exception) {
            // Fallback navigation
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    // Helper Methods for Clean Code
    private fun clearErrors() {
        usernameInput.error = null
        passwordInput.error = null
    }

    private fun showFieldError(field: EditText, message: String) {
        field.error = message
        field.requestFocus()
    }

    private fun showLockoutMessage() {
        val remainingTime = (lockoutDurationMs - (System.currentTimeMillis() - lastFailedAttemptTime)) / 1000
        Toast.makeText(this, "Account locked. Try again in ${remainingTime}s", Toast.LENGTH_LONG).show()
    }

    private fun showMaxAttemptsMessage() {
        Toast.makeText(this, "Maximum login attempts reached. Please wait.", Toast.LENGTH_LONG).show()
    }

    private fun showSuccessMessage() {
        Toast.makeText(this, "✓ Login successful! Welcome back.", Toast.LENGTH_SHORT).show()
    }

    private fun showFailureMessage(message: String) {
        val remaining = maxLoginAttempts - loginAttempts
        val displayMessage = when {
            remaining <= 0 -> "Account temporarily locked due to failed attempts"
            remaining <= 2 -> "$message\n⚠️ Warning: Only $remaining attempts remaining"
            else -> "$message ($remaining attempts left)"
        }
        Toast.makeText(this, displayMessage, Toast.LENGTH_LONG).show()
    }

    private fun showSessionRestoredMessage() {
        Toast.makeText(this, "Session restored. Welcome back!", Toast.LENGTH_SHORT).show()
    }

    private fun isInLockoutPeriod(): Boolean {
        return loginAttempts >= maxLoginAttempts &&
                (System.currentTimeMillis() - lastFailedAttemptTime) < lockoutDurationMs
    }

    private fun resetSecurityState() {
        loginAttempts = 0
        lastFailedAttemptTime = 0L
    }

    private fun hasValidSession(): Boolean {
        return try {
            val prefs = getSharedPreferences("secure_auth", MODE_PRIVATE)
            val loginTime = prefs.getLong("login_time", 0L)
            val token = prefs.getString("backup_token", null)

            if (token.isNullOrBlank()) return false

            // Check session validity (24 hours)
            val sessionAge = System.currentTimeMillis() - loginTime
            val maxSessionAge = 24 * 60 * 60 * 1000L // 24 hours

            sessionAge < maxSessionAge
        } catch (e: Exception) {
            false
        }
    }

    private fun saveTokensSecurely(token: String) {
        try {
            // Primary storage (maintains compatibility)
            getSharedPreferences("auth_prefs", MODE_PRIVATE).edit()
                .putString("access_token", token)
                .apply()

            // Enhanced secure storage
            getSharedPreferences("secure_auth", MODE_PRIVATE).edit()
                .putString("backup_token", token)
                .putLong("login_time", System.currentTimeMillis())
                .putString("device_id", getUniqueDeviceId())
                .putString("login_method", "password")
                .apply()
        } catch (e: Exception) {
            // Fallback to original method
            saveToken(token)
        }
    }

    private fun getUniqueDeviceId(): String {
        return try {
            android.provider.Settings.Secure.getString(
                contentResolver,
                android.provider.Settings.Secure.ANDROID_ID
            ) ?: "unknown"
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun hideKeyboard() {
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        } catch (e: Exception) {
            // Ignore keyboard hiding errors
        }
    }

    // Original compatibility method
    private fun saveToken(token: String) {
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        sharedPref.edit()
            .putString("access_token", token)
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        isLoggingIn = false
        handler.removeCallbacksAndMessages(null)
    }

    override fun onPause() {
        super.onPause()
        // Clear sensitive data from UI when app goes to background
        if (!isLoggingIn) {
            passwordInput.text?.clear()
        }
    }
}