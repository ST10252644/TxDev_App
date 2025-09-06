package com.iie.st10089153.txdevsystems_app.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

import com.iie.st10089153.txdevsystems_app.ui.login.LoginActivity
import com.iie.st10089153.txdevsystems_app.R

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val splashTime: Long = 2000 // 2 seconds
    private var keepSplashScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        // For Android 12+, install and immediately dismiss the system splash screen
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val splashScreen = installSplashScreen()
            splashScreen.setKeepOnScreenCondition { keepSplashScreen }
        }

        super.onCreate(savedInstanceState)

        // Make it full screen and hide system UI
        setupFullscreen()

        setContentView(R.layout.activity_splash)

        // Dismiss system splash screen immediately to show your custom one
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            keepSplashScreen = false
        }

        // Start your custom splash logic
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToLogin()
        }, splashTime)
    }

    private fun setupFullscreen() {
        // Hide status bar and navigation bar for full immersive experience
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // For newer Android versions, use system UI visibility
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.hide(android.view.WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        // Add custom transition animation
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

        finish()
    }

//    override fun onBackPressed() {
//        // Disable back button on splash screen
//        // Do nothing
//    }
}