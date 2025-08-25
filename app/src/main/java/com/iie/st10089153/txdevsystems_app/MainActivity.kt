package com.iie.st10089153.txdevsystems_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iie.st10089153.txdevsystems_app.databinding.ActivityMainBinding
import com.iie.st10089153.txdevsystems_app.ui.login.LoginActivity

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  Check if user is logged in
        val sharedPref = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)
        if (token.isNullOrEmpty()) {
            // No token → redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        //  Inflate layout and set toolbar
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topNav)

        // Setup NavController
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val navView: BottomNavigationView = binding.navView
        navView.setupWithNavController(navController)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_dashboard,
                R.id.navigation_profile,
                R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 🔹 Handle toolbar visibility & actions based on destination
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            when (destination.id) {
                R.id.navigation_home -> {
                    binding.topNav.visibility = View.GONE
                }
                R.id.navigation_notifications -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "Settings"
                    binding.topNavRightButton.visibility = View.GONE
                }
                R.id.navigation_profile -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "Edit Profile"
                    binding.topNavRightButton.visibility = View.GONE
                }
                R.id.navigation_dashboard -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "Dashboard"
                    binding.topNavRightButton.visibility = View.VISIBLE
                    binding.topNavRightButton.setImageResource(R.drawable.ic_settings)

                    binding.topNavRightButton.setOnClickListener {
                        navController.navigate(R.id.navigation_profile)
                    }
                }
                // Add other destinations here (analysis, add, categories, etc.)

                else -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.GONE
                    binding.topNavTitle.text = "App"
                    binding.topNavRightButton.visibility = View.GONE
                }
            }
        }

        // 🔹 Back button action
        binding.topNavBackButton.setOnClickListener {
            navController.navigateUp()
        }

        // Optional: navigate to home if intent says so
        if (intent.getBooleanExtra("navigateToHome", false)) {
            navController.navigate(R.id.navigation_home)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
