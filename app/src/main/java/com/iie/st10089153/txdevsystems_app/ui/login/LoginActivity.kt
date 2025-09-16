package com.iie.st10089153.txdevsystems_app.ui.login

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginButton = findViewById(R.id.loginButton)
        usernameInput = findViewById(R.id.usernameInput)
        passwordInput = findViewById(R.id.passwordInput)
        togglePasswordButton = findViewById(R.id.btnTogglePassword)

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
            passwordInput.setSelection(passwordInput.text.length) // move cursor to end
        }
    }

    private fun performLogin(username: String, password: String) {
        val call = RetrofitClient.getAuthApi(this).login(username = username, password = password)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.access_token
                    saveToken(token)
                    // TODO: Save token securely (SharedPreferences or encrypted storage)

                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()


                    // Navigate to MainActivity
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "Login failed: Invalid credentials", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(this@LoginActivity, "Login failed: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun saveToken(token: String) {
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        sharedPref.edit()
            .putString("access_token", token)
            .apply()
    }

}
