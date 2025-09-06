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
import com.iie.st10089153.txdevsystems_app.network.Api.UpdateConfigRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.chart.resolveImeiFlexible
import com.iie.st10089153.txdevsystems_app.ui.device.models.TempThresholdRequest
import com.iie.st10089153.txdevsystems_app.network.Api.UpdateUnitNameRequest
import kotlinx.coroutines.launch

class DeviceSettingsFragment : Fragment() {

    private var _binding: FragmentDeviceSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var api: DeviceApi

    private var currentImei: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeviceSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        api = RetrofitClient.getDeviceApi(requireContext())

        // ✅ Flexible IMEI resolution
        currentImei = resolveImeiFlexible()
        Log.d("DeviceSettingsFragment", "Received IMEI via flexible lookup: $currentImei")

        currentImei?.let { loadDeviceSettings(it) }
            ?: run {
                Log.e("DeviceSettingsFragment", "IMEI is null, cannot load settings")
                Toast.makeText(requireContext(), "IMEI missing", Toast.LENGTH_SHORT).show()
            }

        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.btnSaveChanges.setOnClickListener { saveDeviceSettings() }
    }

    private fun loadDeviceSettings(imei: String) {
        Log.d("DeviceSettingsFragment", "loadDeviceSettings called with IMEI: $imei")
        lifecycleScope.launch {
            try {
                val response = api.getConfigByImei(ConfigByImeiRequest(imei))
                if (response.isSuccessful) {
                    response.body()?.let { config ->

                        // Device ID + Name
                        binding.tvDeviceID.text = config.imei
                        binding.etDeviceName.setText(config.unit_id)

                        // Temps
                        binding.etHighTemp.setText(config.temp_max ?: "")
                        binding.etLowTemp.setText(config.temp_min ?: "")

                        // Door alert time
                        val doorHour = config.door_alarm_hour ?: "0"
                        val doorMin = config.door_alarm_min ?: "0"
                        binding.etDoorAlertTime.setText("$doorHour:$doorMin")

                        // Spinner (make sure it's initialized before this)
                        val spinnerOptions = listOf("NO", "NC")
                        val adapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            spinnerOptions
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        binding.spinnerDoorType.adapter = adapter

                        // Map polarity (your API sends "0" or "1")
                        val polarity = if (config.switch_polarity == "1") "NO" else "NC"
                        val spinnerIndex = spinnerOptions.indexOf(polarity)
                        if (spinnerIndex >= 0) {
                            binding.spinnerDoorType.setSelection(spinnerIndex)
                        }

                        Log.d("DeviceSettingsFragment", "Device config loaded successfully")
                    } ?: Log.e("DeviceSettingsFragment", "Response body is null")
                } else {
                    Log.e("DeviceSettingsFragment", "API call failed: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load device settings", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DeviceSettingsFragment", "Exception during API call: ${e.localizedMessage}", e)
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

        // 1️⃣ Update unit name
        val newNameRequest = UpdateUnitNameRequest(
            imei = imei,
            new_name = unitName
        )

        lifecycleScope.launch {
            try {
                val responseName = api.updateUnitName(newNameRequest)
                if (responseName.isSuccessful) {
                    Log.d("DeviceSettingsFragment", "Unit name updated successfully")
                } else {
                    Log.e(
                        "DeviceSettingsFragment",
                        "Failed to update name: ${responseName.errorBody()?.string()}"
                    )
                }
            } catch (e: Exception) {
                Log.e("DeviceSettingsFragment", "Exception updating name: ${e.localizedMessage}", e)
            }
        }

        // 2️⃣ Update temperature thresholds
        val tempRequest = TempThresholdRequest(
            imei = imei,
            max = maxTemp,
            min = minTemp
        )

        lifecycleScope.launch {
            try {
                val responseTemp = api.setTempThresholds(tempRequest)
                if (responseTemp.isSuccessful) {
                    Log.d("DeviceSettingsFragment", "Temperature thresholds updated successfully")
                    Toast.makeText(requireContext(), "Device settings updated", Toast.LENGTH_SHORT).show()
                } else {
                    val errorMsg = responseTemp.errorBody()?.string()
                    Log.e("DeviceSettingsFragment", "Temp update failed: $errorMsg")
                    Toast.makeText(requireContext(), "Failed to update temperature", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DeviceSettingsFragment", "Exception updating temps: ${e.localizedMessage}", e)
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }

        // 3️⃣ Door alert updates would need a separate endpoint if available
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
