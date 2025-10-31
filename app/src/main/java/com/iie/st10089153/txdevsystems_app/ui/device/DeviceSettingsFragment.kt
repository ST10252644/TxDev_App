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
import com.iie.st10089153.txdevsystems_app.ui.device.models.DoorAlarmMinRequest
import com.iie.st10089153.txdevsystems_app.ui.device.models.SwitchPolarityRequest
import com.iie.st10089153.txdevsystems_app.ui.device.models.TempThresholdRequest
import kotlinx.coroutines.launch

class DeviceSettingsFragment : Fragment() {

    private val TAG = "DeviceSettingsFragment"
    private var _binding: FragmentDeviceSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var api: DeviceApi
    private var currentImei: String? = null
    private var isEditMode = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeviceSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        api = RetrofitClient.getDeviceApi(requireContext())
        currentImei = resolveImeiFlexible()

        currentImei?.let { loadDeviceSettings(it) }
            ?: Toast.makeText(requireContext(), "IMEI missing", Toast.LENGTH_SHORT).show()

        setEditMode(false) // Start in view-only mode
        binding.btnSaveChanges.setOnClickListener { saveDeviceSettings() }
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
        binding.etDeviceName.isEnabled = enabled
        binding.etHighTemp.isEnabled = enabled
        binding.etLowTemp.isEnabled = enabled
        binding.etDoorAlertTime.isEnabled = enabled
        binding.spinnerDoorType.isEnabled = enabled
        binding.btnSaveChanges.visibility = if (enabled) View.VISIBLE else View.GONE

        refreshDisplayForMode()
        notifyParentEditModeChanged()
    }

    fun isInEditMode(): Boolean = isEditMode

    private fun notifyParentEditModeChanged() {
        (activity as? OnEditModeChangeListener)?.onEditModeChanged(isEditMode)
    }

    private fun refreshDisplayForMode() {
        val maxTemp = binding.etHighTemp.text.toString().replace("°C", "").trim()
        val minTemp = binding.etLowTemp.text.toString().replace("°C", "").trim()
        val doorTime = binding.etDoorAlertTime.text.toString()
            .replace(" minutes", "")
            .replace(" minute", "")
            .trim()

        binding.etHighTemp.setText(if (isEditMode) maxTemp else "$maxTemp°C")
        binding.etLowTemp.setText(if (isEditMode) minTemp else "$minTemp°C")

        // Format door alert time properly
        val minuteLabel = if (doorTime == "1") "minute" else "minutes"
        binding.etDoorAlertTime.setText(if (isEditMode) doorTime else "$doorTime $minuteLabel")
    }

    private fun loadDeviceSettings(imei: String) {
        Log.d(TAG, "Loading device settings for IMEI: $imei")
        lifecycleScope.launch {
            try {
                val response = api.getConfigByImei(ConfigByImeiRequest(imei))
                if (response.isSuccessful) {
                    response.body()?.let { config ->
                        Log.d(TAG, "Loaded config - temp_min: ${config.temp_min}, temp_max: ${config.temp_max}")

                        binding.tvDeviceID.text = config.imei
                        binding.etDeviceName.setText(config.unit_id)

                        val maxTempText = config.temp_max ?: ""
                        val minTempText = config.temp_min ?: ""

                        // Calculate total minutes from hours and minutes
                        val doorHour = config.door_alarm_hour?.toIntOrNull() ?: 0
                        val doorMin = config.door_alarm_min?.toIntOrNull() ?: 0
                        val totalMinutes = (doorHour * 60) + doorMin

                        // Format the display based on mode
                        val minuteLabel = if (totalMinutes == 1) "minute" else "minutes"
                        val doorTimeDisplay = if (isEditMode) {
                            totalMinutes.toString()
                        } else {
                            "$totalMinutes $minuteLabel"
                        }

                        binding.etHighTemp.setText(if (isEditMode) maxTempText else "$maxTempText°C")
                        binding.etLowTemp.setText(if (isEditMode) minTempText else "$minTempText°C")
                        binding.etDoorAlertTime.setText(doorTimeDisplay)

                        val spinnerOptions = listOf("No Open", "No Close")
                        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, spinnerOptions)
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        binding.spinnerDoorType.adapter = adapter

                        val polarity = if (config.switch_polarity == "1") "No Open" else "No Close"
                        val spinnerIndex = spinnerOptions.indexOf(polarity)
                        if (spinnerIndex >= 0) binding.spinnerDoorType.setSelection(spinnerIndex)
                    }
                } else {
                    Log.e(TAG, "Failed to load settings: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load device settings", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading settings", e)
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveDeviceSettings() {
        val imei = currentImei ?: run {
            Log.e(TAG, "Cannot save: IMEI is null")
            return
        }

        val unitName = binding.etDeviceName.text.toString()
        // ✅ Changed: Keep as String instead of converting to Int
        val maxTempStr = binding.etHighTemp.text.toString()
        val minTempStr = binding.etLowTemp.text.toString()

        Log.d(TAG, "Saving settings - High Temp (max): $maxTempStr, Low Temp (min): $minTempStr")

        // Get total minutes entered by user
        val totalMinutes = binding.etDoorAlertTime.text.toString()
            .replace(" minutes", "")
            .replace(" minute", "")
            .trim()
            .toIntOrNull() ?: 0

        // Convert to hours and minutes for API
        val doorHour = (totalMinutes / 60).toString()
        val doorMin = (totalMinutes % 60).toString()

        val switchPolarity = if (binding.spinnerDoorType.selectedItem.toString() == "No Open") "1" else "0"

        lifecycleScope.launch {
            try {
                Log.d(TAG, "Calling API - setTempThresholds(imei=$imei, maxTemp=$maxTempStr, minTemp=$minTempStr)")

                val nameResponse = api.updateUnitName(UpdateUnitNameRequest(imei, unitName))
                Log.d(TAG, "updateUnitName response: ${nameResponse.code()}")

                val tempResponse = api.setTempThresholds(TempThresholdRequest(imei, maxTempStr, minTempStr))
                if (tempResponse.isSuccessful) {
                    Log.d(TAG, "setTempThresholds response: ${tempResponse.code()} - SUCCESS")
                } else {
                    val errorBody = tempResponse.errorBody()?.string()
                    Log.e(TAG, "setTempThresholds FAILED: ${tempResponse.code()}")
                    Log.e(TAG, "Error body: $errorBody")
                    Toast.makeText(requireContext(), "Failed to update temperature: $errorBody", Toast.LENGTH_LONG).show()
                }

                val doorResponse = api.setDoorAlarmMin(DoorAlarmMinRequest(imei, doorMin))
                Log.d(TAG, "setDoorAlarmMin response: ${doorResponse.code()}")

                val polarityResponse = api.setSwitchPolarity(SwitchPolarityRequest(imei, switchPolarity))
                Log.d(TAG, "setSwitchPolarity response: ${polarityResponse.code()}")

                if (tempResponse.isSuccessful) {
                    Toast.makeText(requireContext(), "Device settings updated", Toast.LENGTH_SHORT).show()
                    // Switch to view mode and reload updated values
                    setEditMode(false)
                    currentImei?.let { loadDeviceSettings(it) }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception updating settings: ${e.localizedMessage}", e)
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    interface OnEditModeChangeListener {
        fun onEditModeChanged(isEditMode: Boolean)
    }
}