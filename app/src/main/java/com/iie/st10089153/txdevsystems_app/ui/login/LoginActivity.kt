package com.iie.st10089153.txdevsystems_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.iie.st10089153.txdevsystems_app.MainActivity
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var loginButton: Button
    private lateinit var usernameInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var togglePasswordButton: ImageButton
    private lateinit var rememberMeCheckbox: CheckBox
    private lateinit var sessionManager: SessionManager
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn() && sessionManager.isRememberMeEnabled()) {
            navigateToMain()
            return
        }

        loginButton = findViewById(R.id.loginButton)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        togglePasswordButton = findViewById(R.id.btnTogglePassword)
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (username.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(username, password)
        }

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
        val call = RetrofitClient.getAuthApi(this).login(username = username, password = password)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val rememberMe = rememberMeCheckbox.isChecked

                    sessionManager.saveLoginSession(
                        accessToken = loginResponse.access_token,
                        refreshToken = loginResponse.refresh_token,
                        userId = null,
                        userEmail = username,
                        rememberMe = rememberMe
                    )

                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()

                    navigateToMain()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: Invalid credentials", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Login failed: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun navigateToMain() {
        val intent = Intent(this@LoginActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}