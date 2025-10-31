package com.iie.st10089153.txdevsystems_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.iie.st10089153.fragments.ProfileFragment
import com.iie.st10089153.txdevsystems_app.databinding.ActivityMainBinding
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsApi
import com.iie.st10089153.txdevsystems_app.network.Api.FcmTokenRequest
import com.iie.st10089153.txdevsystems_app.network.Api.NotificationsApi
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.device.DeviceSettingsFragment
import com.iie.st10089153.txdevsystems_app.ui.login.LoginActivity
import com.iie.st10089153.txdevsystems_app.utils.SessionManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), DeviceSettingsFragment.OnEditModeChangeListener {

    private val logoutTime: Long = 5 * 60 * 1000
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var sessionManager: SessionManager

    private val logoutRunnable = Runnable {
        if (!sessionManager.isRememberMeEnabled()) {
            sessionManager.clearAutoLogout()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        resetLogoutTimer()
    }

    private fun resetLogoutTimer() {
        handler.removeCallbacks(logoutRunnable)
        handler.postDelayed(logoutRunnable, logoutTime)
    }

    override fun onPause() {
        super.onPause()
        if (!sessionManager.isRememberMeEnabled()) {
            handler.removeCallbacks(logoutRunnable)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!sessionManager.isRememberMeEnabled()) {
            resetLogoutTimer()
        }
    }

    private lateinit var binding: ActivityMainBinding

    private var isInEditMode = false
    private lateinit var api: AvailableUnitsApi
    var currentUnitName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.topNav)

        getFCMToken()

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val navView: BottomNavigationView = binding.navView

        // Configure bottom navigation colors
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),   // Selected
            intArrayOf(-android.R.attr.state_checked)   // Unselected
        )

        val colors = intArrayOf(
            ContextCompat.getColor(this, R.color.tx_green),    // Selected (Green)
            ContextCompat.getColor(this, R.color.uneditable)   // Unselected (Gray)
        )

        val colorStateList = android.content.res.ColorStateList(states, colors)
        navView.itemIconTintList = colorStateList
        navView.itemTextColor = colorStateList

        navView.setupWithNavController(navController)

        // Manual click listener to ensure proper highlighting
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_notifications -> {
                    navController.navigate(R.id.navigation_notifications)
                    true
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile)
                    true
                }
                else -> false
            }
        }

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

                R.id.navigation_reports -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Reports"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

                R.id.navigation_temperature_chart -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Temperature Chart"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

                R.id.navigation_door_history_chart -> {
                    binding.topNav.visibility = View.VISIBLE
                    binding.topNavBackButton.visibility = View.VISIBLE
                    binding.topNavTitle.text = "$deviceName Door Chart"
                    binding.topNavRightButton.visibility = View.GONE
                    isInEditMode = false
                }

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


        binding.topNavBackButton.setOnClickListener {
            val fragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
                ?.childFragmentManager?.fragments?.firstOrNull()

            when {
                fragment is ProfileFragment && fragment.childFragmentManager.backStackEntryCount > 0 -> {
                    fragment.childFragmentManager.popBackStack()
                    binding.topNavTitle.text = "Profile"
                    binding.topNavRightButton.visibility = View.VISIBLE
                }
                fragment is DeviceSettingsFragment && fragment.isResumed -> {
                    if (fragment.isInEditMode()) {
                        fragment.setEditMode(false)
                    } else {
                        navController.navigateUp()
                    }
                }
                else -> {
                    navController.navigateUp()
                }
            }
        }

        if (intent.getBooleanExtra("navigateToHome", false)) {
            navController.navigate(R.id.navigation_home)
        }
    }

    private fun getFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            Log.d("FCM", "FCM Token: $token")

            sendTokenToServer(token)
        }
    }

    private fun sendTokenToServer(fcmToken: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitClient.getInstance(this@MainActivity).create(NotificationsApi::class.java)
                val request = FcmTokenRequest(
                    token = fcmToken,
                    device_type = "android"
                )
                val response = api.registerFcmToken(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        Log.d("FCM", "Token registered successfully: ${result?.message}")
                        Toast.makeText(this@MainActivity, "Notifications enabled", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("FCM", "Failed to register token: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error registering token", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to enable notifications", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun performLogout() {
        sessionManager.logout()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showCustomPopupMenu(view: View, currentImei: String?) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.custom_popup_menu_layout, null)

        val popupWindow = PopupWindow(
            popupView,
            247.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.popup_menu_background))
        popupWindow.elevation = 8f

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

        popupWindow.showAsDropDown(view, -200, 0)
    }

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

    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    override fun onEditModeChanged(isEditMode: Boolean) {
        this.isInEditMode = isEditMode

        val deviceName = currentUnitName ?: "Device"

        if (isEditMode) {
            binding.topNavRightButton.visibility = View.GONE
            binding.topNavTitle.text = "$deviceName Edit"
        } else {
            binding.topNavRightButton.visibility = View.VISIBLE
            binding.topNavRightButton.setImageResource(R.drawable.ic_edit)
            binding.topNavTitle.text = "$deviceName Settings"
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun setTopNavTitle(name: String) {
        binding.topNavTitle.text = name
    }

}