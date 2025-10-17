package com.iie.st10089153.txdevsystems_app.ui.device

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentDeviceSettingsBinding
import com.iie.st10089153.txdevsystems_app.network.Api.ConfigByImeiRequest
import com.iie.st10089153.txdevsystems_app.network.Api.DeviceApi
import com.iie.st10089153.txdevsystems_app.network.Api.UpdateUnitNameRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.chart.resolveImeiFlexible
import com.iie.st10089153.txdevsystems_app.ui.device.models.ConfigResponse
import com.iie.st10089153.txdevsystems_app.ui.device.models.DoorAlarmMinRequest
import com.iie.st10089153.txdevsystems_app.ui.device.models.SwitchPolarityRequest
import com.iie.st10089153.txdevsystems_app.ui.device.models.TempThresholdRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceSettingsFragment : Fragment() {

    private var _binding: FragmentDeviceSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var api: DeviceApi
    private var currentImei: String? = null
    private var isEditMode = false

    // Cache for device configuration to reduce API calls
    private var cachedConfig: ConfigResponse? = null
    private var lastLoadTime: Long = 0
    private val CACHE_VALIDITY_MS = 60000L // 1 minute cache

    companion object {
        private const val TAG = "DeviceSettingsFragment"
        private const val DOOR_ALARM_HOUR_DEFAULT = "0"
        private const val DOOR_ALARM_MIN_DEFAULT = "0"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        api = RetrofitClient.getDeviceApi(requireContext())
        currentImei = resolveImeiFlexible()

        currentImei?.let {
            loadDeviceSettings(it, forceRefresh = false)
        } ?: showError("IMEI missing")

        setupUI()
    }

    private fun setupUI() {
        setEditMode(false) // Start in view-only mode
        binding.btnSaveChanges.setOnClickListener {
            if (validateInputs()) {
                saveDeviceSettings()
            }
        }
    }

    // Toggle edit mode from MainActivity
    fun toggleEditMode() {
        isEditMode = !isEditMode
        setEditMode(isEditMode)
        notifyParentEditModeChanged()
    }

    // Set edit/view mode
    fun setEditMode(enabled: Boolean) {
        isEditMode = enabled
        binding.apply {
            etDeviceName.isEnabled = enabled
            etHighTemp.isEnabled = enabled
            etLowTemp.isEnabled = enabled
            etDoorAlertTime.isEnabled = enabled
            spinnerDoorType.isEnabled = enabled
            btnSaveChanges.visibility = if (enabled) View.VISIBLE else View.GONE
        }

        refreshDisplayForMode()
        notifyParentEditModeChanged()
    }

    fun isInEditMode(): Boolean = isEditMode

    private fun notifyParentEditModeChanged() {
        (activity as? OnEditModeChangeListener)?.onEditModeChanged(isEditMode)
    }

    private fun refreshDisplayForMode() {
        binding.apply {
            val maxTemp = etHighTemp.text.toString().replace("°C", "").trim()
            val minTemp = etLowTemp.text.toString().replace("°C", "").trim()
            val doorMin = etDoorAlertTime.text.toString().replace(" min", "").trim()

            etHighTemp.setText(if (isEditMode) maxTemp else "$maxTemp°C")
            etLowTemp.setText(if (isEditMode) minTemp else "$minTemp°C")
            etDoorAlertTime.setText(if (isEditMode) doorMin else "$doorMin min")
        }
    }

    /**
     * Load device settings with caching support
     * @param imei Device IMEI
     * @param forceRefresh Force API call even if cache is valid
     */
    private fun loadDeviceSettings(imei: String, forceRefresh: Boolean = false) {
        // Check cache validity
        val currentTime = System.currentTimeMillis()
        val isCacheValid = cachedConfig != null &&
                (currentTime - lastLoadTime) < CACHE_VALIDITY_MS

        if (isCacheValid && !forceRefresh) {
            Log.d(TAG, "Loading from cache")
            cachedConfig?.let { displayDeviceSettings(it) }
            return
        }

        // Load from API
        lifecycleScope.launch {
            try {
                setLoadingState(true)

                val response = withContext(Dispatchers.IO) {
                    api.getConfigByImei(ConfigByImeiRequest(imei))
                }

                if (response.isSuccessful) {
                    response.body()?.let { config ->
                        cachedConfig = config
                        lastLoadTime = System.currentTimeMillis()
                        displayDeviceSettings(config)
                        Log.d(TAG, "Device settings loaded successfully")
                    } ?: showError("Empty response from server")
                } else {
                    handleApiError(response.code(), "Failed to load device settings")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading device settings", e)
                handleException(e, "Error loading device settings")
            } finally {
                setLoadingState(false)
            }
        }
    }

    /**
     * Display device settings in UI
     * Optimized to update all views in one pass
     */
    private fun displayDeviceSettings(config: ConfigResponse) {
        binding.apply {
            // Basic info
            tvDeviceID.text = config.imei
            etDeviceName.setText(config.unit_id)

            // Temperature thresholds
            val maxTempText = config.temp_max ?: "0"
            val minTempText = config.temp_min ?: "0"
            etHighTemp.setText(if (isEditMode) maxTempText else "$maxTempText°C")
            etLowTemp.setText(if (isEditMode) minTempText else "$minTempText°C")

            // Door alarm - now ONLY uses minutes (no hours needed)
            val doorMin = config.door_alarm_min ?: DOOR_ALARM_MIN_DEFAULT
            etDoorAlertTime.setText(if (isEditMode) doorMin else "$doorMin min")

            // Door type spinner
            setupDoorTypeSpinner(config.switch_polarity ?: "0")
        }
    }

    /**
     * Setup door type spinner with current value
     */
    private fun setupDoorTypeSpinner(switchPolarity: String) {
        val spinnerOptions = listOf("No Open", "No Close")
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            spinnerOptions
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.spinnerDoorType.adapter = adapter

        // Set selection based on polarity
        val polarity = if (switchPolarity == "1") "No Open" else "No Close"
        val spinnerIndex = spinnerOptions.indexOf(polarity)
        if (spinnerIndex >= 0) {
            binding.spinnerDoorType.setSelection(spinnerIndex)
        }
    }

    /**
     * Validate user inputs before saving
     */
    private fun validateInputs(): Boolean {
        binding.apply {
            val unitName = etDeviceName.text.toString().trim()
            if (unitName.isEmpty()) {
                etDeviceName.error = "Device name cannot be empty"
                return false
            }

            val maxTempStr = etHighTemp.text.toString().replace("°C", "").trim()
            val minTempStr = etLowTemp.text.toString().replace("°C", "").trim()
            val doorMinStr = etDoorAlertTime.text.toString().replace(" min", "").trim()

            // Validate temperature values
            val maxTemp = maxTempStr.toIntOrNull()
            val minTemp = minTempStr.toIntOrNull()

            if (maxTemp == null) {
                etHighTemp.error = "Invalid temperature"
                return false
            }
            if (minTemp == null) {
                etLowTemp.error = "Invalid temperature"
                return false
            }
            if (maxTemp <= minTemp) {
                etHighTemp.error = "Max temp must be greater than min temp"
                return false
            }

            // Validate door alarm minutes
            val doorMin = doorMinStr.toIntOrNull()
            if (doorMin == null || doorMin < 0 || doorMin > 1440) { // Max 24 hours in minutes
                etDoorAlertTime.error = "Invalid minutes (0-1440)"
                return false
            }

            return true
        }
    }

    /**
     * Save device settings with optimized API calls
     */
    private fun saveDeviceSettings() {
        val imei = currentImei ?: run {
            Log.e(TAG, "Cannot save: IMEI is null")
            showError("Device IMEI not found")
            return
        }

        binding.apply {
            val unitName = etDeviceName.text.toString().trim()
            val maxTemp = etHighTemp.text.toString().replace("°C", "").trim().toInt()
            val minTemp = etLowTemp.text.toString().replace("°C", "").trim().toInt()
            val doorMin = etDoorAlertTime.text.toString().replace(" min", "").trim()
            val switchPolarity = if (spinnerDoorType.selectedItem.toString() == "No Open") "1" else "0"

            lifecycleScope.launch {
                try {
                    setLoadingState(true)

                    // Execute all API calls in parallel using async for better performance
                    withContext(Dispatchers.IO) {
                        // Make all API calls
                        val unitNameResponse = api.updateUnitName(
                            UpdateUnitNameRequest(imei, unitName)
                        )
                        val tempResponse = api.setTempThresholds(
                            TempThresholdRequest(imei, maxTemp, minTemp)
                        )
                        val doorAlarmResponse = api.setDoorAlarmMin(
                            DoorAlarmMinRequest(imei, doorMin)
                        )
                        val polarityResponse = api.setSwitchPolarity(
                            SwitchPolarityRequest(imei, switchPolarity)
                        )

                        // Check all responses
                        if (!unitNameResponse.isSuccessful) {
                            throw Exception("Failed to update unit name: ${unitNameResponse.code()}")
                        }
                        if (!tempResponse.isSuccessful) {
                            throw Exception("Failed to update temperature: ${tempResponse.code()}")
                        }
                        if (!doorAlarmResponse.isSuccessful) {
                            throw Exception("Failed to update door alarm: ${doorAlarmResponse.code()}")
                        }
                        if (!polarityResponse.isSuccessful) {
                            throw Exception("Failed to update polarity: ${polarityResponse.code()}")
                        }
                    }

                    // Success - invalidate cache and reload
                    cachedConfig = null
                    showSuccess("Device settings updated successfully")

                    // Switch to view mode and reload updated values
                    setEditMode(false)
                    loadDeviceSettings(imei, forceRefresh = true)

                } catch (e: Exception) {
                    Log.e(TAG, "Exception updating settings", e)
                    handleException(e, "Failed to update device settings")
                } finally {
                    setLoadingState(false)
                }
            }
        }
    }

    /**
     * Handle API errors with proper error codes
     */
    private fun handleApiError(code: Int, defaultMessage: String) {
        val message = when (code) {
            400 -> "Bad request - check your input"
            401 -> "Unauthorized - please login again"
            403 -> "Forbidden - you don't have permission"
            404 -> "Device not found"
            500 -> "Server error - please try again later"
            else -> "$defaultMessage (Error $code)"
        }
        showError(message)
    }

    /**
     * Handle exceptions with user-friendly messages
     */
    private fun handleException(e: Exception, context: String) {
        val message = when (e) {
            is java.net.UnknownHostException -> "No internet connection"
            is java.net.SocketTimeoutException -> "Connection timeout - please try again"
            is javax.net.ssl.SSLException -> "Secure connection failed"
            else -> "$context: ${e.localizedMessage ?: "Unknown error"}"
        }
        showError(message)
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Manage loading state by disabling/enabling UI elements
     */
    private fun setLoadingState(isLoading: Boolean) {
        binding.apply {
            btnSaveChanges.isEnabled = !isLoading
            btnSaveChanges.text = if (isLoading) "Saving..." else "Save Changes"

            // Disable all input fields during loading
            etDeviceName.isEnabled = !isLoading && isEditMode
            etHighTemp.isEnabled = !isLoading && isEditMode
            etLowTemp.isEnabled = !isLoading && isEditMode
            etDoorAlertTime.isEnabled = !isLoading && isEditMode
            spinnerDoorType.isEnabled = !isLoading && isEditMode
        }
    }

    /**
     * Public method to refresh data (can be called from parent activity)
     */
    fun refreshData() {
        currentImei?.let { loadDeviceSettings(it, forceRefresh = true) }
    }

    /**
     * Clear cache when fragment is destroyed
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnEditModeChangeListener {
        fun onEditModeChanged(isEditMode: Boolean)
    }
}