package com.iie.st10089153.txdevsystems_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.os.bundleOf
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

                    // 🔹 Show popup menu when clicking the settings icon
                    binding.topNavRightButton.setOnClickListener { view ->
                        val popup = PopupMenu(this, view)
                        popup.menuInflater.inflate(R.menu.dashboard_settings_menu, popup.menu)

                        popup.setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.action_device_settings -> {
                                    // Get IMEI from current destination arguments
                                    val currentImei = arguments?.getString("IMEI")
                                    if (currentImei != null) {
                                        navController.navigate(
                                            R.id.action_dashboard_to_device_settings,
                                            bundleOf("IMEI" to currentImei)
                                        )
                                    }
                                    true
                                }
                                R.id.action_view_charts -> {
                                    // Show second popup for charts
                                    val chartsPopup = PopupMenu(this, view)
                                    chartsPopup.menuInflater.inflate(R.menu.charts_menu, chartsPopup.menu)
                                    chartsPopup.setOnMenuItemClickListener { chartItem ->
                                        val currentImei = arguments?.getString("IMEI")
                                        if (currentImei != null) {
                                            val chartBundle = bundleOf("IMEI" to currentImei)
                                            when (chartItem.itemId) {
                                                R.id.action_temperature_chart -> {
                                                    navController.navigate(R.id.navigation_temperature_chart, chartBundle)
                                                    true
                                                }
                                                R.id.action_door_chart -> {
                                                    navController.navigate(R.id.navigation_door_history_chart, chartBundle)
                                                    true
                                                }
                                                R.id.action_battery_chart -> {
                                                    navController.navigate(R.id.navigation_battery_chart, chartBundle)
                                                    true
                                                }
                                                else -> false
                                            }
                                        } else false
                                    }
                                    chartsPopup.show()
                                    true
                                }
                                R.id.action_view_reports -> {
                                    // Navigate to reports with IMEI
                                    val currentImei = arguments?.getString("IMEI")
                                    if (currentImei != null) {
                                        navController.navigate(
                                            R.id.action_dashboard_to_reports,
                                            bundleOf("IMEI" to currentImei)
                                        )
                                    }
                                    true
                                }
                                else -> false
                            }
                        }
                        popup.show()
                    }
                }
                R.id.navigation_reports -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "Data Report"
                    binding.topNavRightButton.visibility = View.GONE
                }
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