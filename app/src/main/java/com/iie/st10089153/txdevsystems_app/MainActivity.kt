package com.iie.st10089153.txdevsystems_app

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupActionBarWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var navNotification: LinearLayout
    private lateinit var navHome: LinearLayout
    private lateinit var navProfile: LinearLayout

    private lateinit var textNotification: TextView
    private lateinit var textHome: TextView
    private lateinit var textProfile: TextView

    private lateinit var iconNotification: ImageView
    private lateinit var iconHome: ImageView
    private lateinit var iconProfile: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        setupActionBarWithNavController(navController)

        // Bottom nav views
        navNotification = findViewById(R.id.nav_notification)
        navHome = findViewById(R.id.nav_home)
        navProfile = findViewById(R.id.nav_profile)

        textNotification = findViewById(R.id.text_notification)
        textHome = findViewById(R.id.text_home_nav)
        textProfile = findViewById(R.id.text_profile)

        iconNotification = findViewById(R.id.icon_notification)
        iconHome = findViewById(R.id.icon_home)
        iconProfile = findViewById(R.id.icon_profile)

        // Listeners
        navNotification.setOnClickListener {
            setActiveTab("notification")
            navController.navigate(R.id.navigation_notifications)
        }

        navHome.setOnClickListener {
            setActiveTab("home")
            navController.navigate(R.id.navigation_home)
        }

        navProfile.setOnClickListener {
            setActiveTab("profile")
            navController.navigate(R.id.navigation_profile)
        }
    }

    private fun setActiveTab(activeTab: String) {
        resetTabColors()

        val activeColor = ContextCompat.getColor(this, R.color.active_nav_color)
        val inactiveColor = ContextCompat.getColor(this, R.color.inactive_nav_color)

        // Reset all icons to default
        iconNotification.setImageResource(R.drawable.elements)
        iconHome.setImageResource(R.drawable.home2)
        iconProfile.setImageResource(R.drawable.user)

        when (activeTab) {
            "notification" -> {
                textNotification.setTextColor(activeColor)
                iconNotification.setImageResource(R.drawable.elements1)
                iconHome.setImageResource(R.drawable.home3) // home changes when other tab active
            }
            "home" -> {
                textHome.setTextColor(activeColor)
                iconHome.setImageResource(R.drawable.home2) // home active icon
            }
            "profile" -> {
                textProfile.setTextColor(activeColor)
                iconProfile.setImageResource(R.drawable.user1)
                iconHome.setImageResource(R.drawable.home3) // home changes when other tab active
            }
        }
    }

    private fun resetTabColors() {
        val inactiveColor = ContextCompat.getColor(this, R.color.inactive_nav_color)
        textNotification.setTextColor(inactiveColor)
        textHome.setTextColor(inactiveColor)
        textProfile.setTextColor(inactiveColor)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}