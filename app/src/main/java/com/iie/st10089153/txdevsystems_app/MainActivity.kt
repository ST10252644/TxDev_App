package com.iie.st10089153.txdevsystems_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.iie.st10089153.fragments.ProfileFragment
import com.iie.st10089153.txdevsystems_app.databinding.ActivityMainBinding
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsApi
import com.iie.st10089153.txdevsystems_app.ui.device.DeviceSettingsFragment
import com.iie.st10089153.txdevsystems_app.ui.login.LoginActivity
import android.os.Handler
import android.os.Looper

class MainActivity : AppCompatActivity(), DeviceSettingsFragment.OnEditModeChangeListener {

    private val logoutTime: Long = 5 * 60 * 1000 // 5 minutes
    private val handler = Handler(Looper.getMainLooper())
    private val logoutRunnable = Runnable {
        //Clear saved session/token first
        getSharedPreferences("auth_prefs", MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        //Navigate back to LoginActivity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        // Reset the timer on user activity
        resetLogoutTimer()
    }

    private fun resetLogoutTimer() {
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, logoutTime)
    }

    override fun onPause() {
        super.onPause()
        // Optional: stop the timer if app goes background
        handler.removeCallbacks(logoutRunnable)
    }

    override fun onResume() {
        super.onResume()
        // Restart timer when coming back
        resetLogoutTimer()
    }

    private lateinit var binding: ActivityMainBinding

    private var isInEditMode = false // Track edit mode state
    private lateinit var api: AvailableUnitsApi
    var currentUnitName: String? = null // cache name after fetch

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                R.id.navigation_notifications,
                R.id.navigation_reports,
                R.id.navigation_battery_chart,
                R.id.navigation_device_settings,
                R.id.navigation_door_history_chart,
                R.id.navigation_temperature_chart
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        // 🔹 Handle toolbar visibility & actions based on destination
        navController.addOnDestinationChangedListener { _, destination, arguments ->
            val deviceName = arguments?.getString("name") ?: currentUnitName ?: "Device"
            when (destination.id) {
                R.id.navigation_home -> {
                    binding.topNav.visibility = View.GONE
                    isInEditMode = false
                }

                R.id.navigation_notifications -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "Notifications"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

                R.id.navigation_profile -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "Profile"
                    binding.topNavRightButton.visibility = View.VISIBLE
                    binding.topNavRightButton.setImageResource(R.drawable.ic_edit)
                    isInEditMode = false

                    binding.topNavRightButton.setOnClickListener {
                        val profileFragment =
                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                                ?.childFragmentManager?.fragments?.firstOrNull()

                        if (profileFragment is ProfileFragment) {
                            profileFragment.showEditProfileFragment()
                            binding.topNavTitle.text = "Edit Profile"
                            binding.topNavRightButton.visibility = View.GONE
                        }
                    }
                }

                R.id.navigation_dashboard -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Dashboard"
                    binding.topNavRightButton.visibility = View.VISIBLE
                    binding.topNavRightButton.setImageResource(R.drawable.ic_settings)
                    isInEditMode = false

                    binding.topNavRightButton.setOnClickListener { view ->
                        val currentImei = arguments?.getString("IMEI")
                        showCustomPopupMenu(view, currentImei)
                    }
                }

                R.id.navigation_device_settings -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Settings"
                    binding.topNavRightButton.visibility = View.VISIBLE
                    binding.topNavRightButton.setImageResource(R.drawable.ic_edit)
                    isInEditMode = false

                    binding.topNavRightButton.setOnClickListener {
                        val fragment =
                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                                ?.childFragmentManager?.fragments?.firstOrNull()
                        if (fragment is DeviceSettingsFragment) {
                            fragment.toggleEditMode()
                        }
                    }

                    binding.root.post {
                        val fragment =
                            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                                ?.childFragmentManager?.fragments?.firstOrNull()
                        if (fragment is DeviceSettingsFragment) {
                            fragment.setEditMode(false)
                        }
                    }
                }

                // ✅ Reports
                R.id.navigation_reports -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Report"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

                // ✅ Temperature Chart
                R.id.navigation_temperature_chart -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Temperature Chart"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

                // ✅ Door Chart
                R.id.navigation_door_history_chart -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Door History"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

                // ✅ Battery Chart
                R.id.navigation_battery_chart -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Battery Chart"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

                else -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.GONE
                    binding.topNavTitle.text = "App"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }
            }
        }


        // 🔹 Back button action
        binding.topNavBackButton.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                ?.childFragmentManager?.fragments?.firstOrNull()

            when {
                // Handle Profile fragment back navigation
                fragment is ProfileFragment && fragment.childFragmentManager.backStackEntryCount > 0 -> {
                    fragment.childFragmentManager.popBackStack()
                    binding.topNavTitle.text = "Profile"
                    binding.topNavRightButton.visibility = View.VISIBLE
                }
                // Handle DeviceSettings fragment back navigation
                fragment is DeviceSettingsFragment && fragment.isResumed -> {
                    if (fragment.isInEditMode()) {
                        // If in edit mode, exit edit mode (return to view mode)
                        fragment.setEditMode(false)
                    } else {
                        // If in view mode, navigate back to dashboard
                        navController.navigateUp()
                    }
                }
                // Default back navigation for other fragments
                else -> {
                    navController.navigateUp()
                }
            }
        }

        // Optional: navigate to home if intent says so
        if (intent.getBooleanExtra("navigateToHome", false)) {
            navController.navigate(R.id.navigation_home)
        }
    }

    // ✅ Custom popup menu for dashboard settings
    private fun showCustomPopupMenu(view: View, currentImei: String?) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.custom_popup_menu_layout, null)

        val popupWindow = PopupWindow(
            popupView,
            247.dpToPx(), // Convert dp to px
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        // Set background and elevation
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_menu_background))
        popupWindow.elevation = 8f

        // Find menu items and set click listeners
        val deviceSettings = popupView.findViewById<View>(R.id.menu_device_settings)
        val viewCharts = popupView.findViewById<View>(R.id.menu_view_charts)
        val viewReports = popupView.findViewById<View>(R.id.menu_view_reports)

        deviceSettings.setOnClickListener {
            if (currentImei != null) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(
                    R.id.action_dashboard_to_device_settings,
                    bundleOf("IMEI" to currentImei)
                )
            }
            popupWindow.dismiss()
        }

        viewCharts.setOnClickListener {
            popupWindow.dismiss()
            showChartsSubmenu(view, currentImei)
        }

        viewReports.setOnClickListener {
            if (currentImei != null) {
                findNavController(R.id.nav_host_fragment_activity_main).navigate(
                    R.id.action_dashboard_to_reports,
                    bundleOf("IMEI" to currentImei)
                )
            }
            popupWindow.dismiss()
        }

        // Show popup
        popupWindow.showAsDropDown(view, -200, 0) // Adjust position as needed
    }

    // ✅ Custom popup menu for charts submenu
    private fun showChartsSubmenu(anchorView: View, currentImei: String?) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.custom_charts_menu_layout, null)

        val popupWindow = PopupWindow(
            popupView,
            247.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_menu_background))
        popupWindow.elevation = 8f

        // Find menu items and set click listeners
        val tempChart = popupView.findViewById<View>(R.id.menu_temperature_chart)
        val doorChart = popupView.findViewById<View>(R.id.menu_door_chart)
        val batteryChart = popupView.findViewById<View>(R.id.menu_battery_chart)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        tempChart.setOnClickListener {
            if (currentImei != null) {
                navController.navigate(R.id.navigation_temperature_chart, bundleOf("IMEI" to currentImei))
            }
            popupWindow.dismiss()
        }

        doorChart.setOnClickListener {
            if (currentImei != null) {
                navController.navigate(R.id.navigation_door_history_chart, bundleOf("IMEI" to currentImei))
            }
            popupWindow.dismiss()
        }

        batteryChart.setOnClickListener {
            if (currentImei != null) {
                navController.navigate(R.id.navigation_battery_chart, bundleOf("IMEI" to currentImei))
            }
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchorView, -200, 0)
    }

    // Extension function to convert dp to px
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    // ✅ Called by DeviceSettingsFragment when edit mode changes
    override fun onEditModeChanged(isEditMode: Boolean) {
        this.isInEditMode = isEditMode

        val deviceName = currentUnitName ?: "Device"

        if (isEditMode) {
            // Entering edit mode
            binding.topNavRightButton.visibility = View.GONE // Hide edit button
            binding.topNavTitle.text = "$deviceName Edit" // Dynamic title
            // Back button stays visible
        } else {
            // Exiting edit mode (back to view mode)
            binding.topNavRightButton.visibility = View.VISIBLE // Show edit button
            binding.topNavRightButton.setImageResource(R.drawable.ic_edit) // Ensure correct icon
            binding.topNavTitle.text = "$deviceName Settings" // Dynamic title
            // Back button stays visible
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    //nav text
    fun setTopNavTitle(name: String) {
        binding.topNavTitle.text = name
    }

}