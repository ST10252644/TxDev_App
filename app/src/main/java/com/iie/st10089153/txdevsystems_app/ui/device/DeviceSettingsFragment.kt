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
        val doorTime = binding.etDoorAlertTime.text.toString().replace(" min", "").trim()

        binding.etHighTemp.setText(if (isEditMode) maxTemp else "$maxTemp°C")
        binding.etLowTemp.setText(if (isEditMode) minTemp else "$minTemp°C")
        binding.etDoorAlertTime.setText(if (isEditMode) doorTime else "$doorTime min")
    }

    private fun loadDeviceSettings(imei: String) {
        lifecycleScope.launch {
            try {
                val response = api.getConfigByImei(ConfigByImeiRequest(imei))
                if (response.isSuccessful) {
                    response.body()?.let { config ->
                        binding.tvDeviceID.text = config.imei
                        binding.etDeviceName.setText(config.unit_id)

                        val maxTempText = config.temp_max ?: ""
                        val minTempText = config.temp_min ?: ""
                        val doorHour = config.door_alarm_hour ?: "0"
                        val doorMin = config.door_alarm_min ?: "0"
                        val doorTimeText = "$doorHour:$doorMin"

                        binding.etHighTemp.setText(if (isEditMode) maxTempText else "$maxTempText°C")
                        binding.etLowTemp.setText(if (isEditMode) minTempText else "$minTempText°C")
                        binding.etDoorAlertTime.setText(if (isEditMode) doorTimeText else "$doorTimeText min")

                        val spinnerOptions = listOf("No Open", "No Close")
                        val adapter = ArrayAdapter(requireContext(), R.layout.spinner_item, spinnerOptions)
                        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
                        binding.spinnerDoorType.adapter = adapter

                        val polarity = if (config.switch_polarity == "1") "No Open" else "No Close"
                        val spinnerIndex = spinnerOptions.indexOf(polarity)
                        if (spinnerIndex >= 0) binding.spinnerDoorType.setSelection(spinnerIndex)
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load device settings", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun saveDeviceSettings() {
        val imei = currentImei ?: run {
            Log.e("DeviceSettingsFragment", "Cannot save: IMEI is null")
            return
        }

        val unitName = binding.etDeviceName.text.toString()
        val maxTemp = binding.etHighTemp.text.toString().toIntOrNull() ?: 0
        val minTemp = binding.etLowTemp.text.toString().toIntOrNull() ?: 0
        val doorTime = binding.etDoorAlertTime.text.toString()
        val doorMin = doorTime.split(":").getOrNull(1)?.trim() ?: "0"
        val switchPolarity = if (binding.spinnerDoorType.selectedItem.toString() == "No Open") "1" else "0"

        lifecycleScope.launch {
            try {
                api.updateUnitName(UpdateUnitNameRequest(imei, unitName))
                api.setTempThresholds(TempThresholdRequest(imei, maxTemp, minTemp))
                api.setDoorAlarmMin(DoorAlarmMinRequest(imei, doorMin))
                api.setSwitchPolarity(SwitchPolarityRequest(imei, switchPolarity))

                Toast.makeText(requireContext(), "Device settings updated", Toast.LENGTH_SHORT).show()

                // Switch to view mode and reload updated values
                setEditMode(false)
                currentImei?.let { loadDeviceSettings(it) }

            } catch (e: Exception) {
                Log.e("DeviceSettingsFragment", "Exception updating settings: ${e.localizedMessage}", e)
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
