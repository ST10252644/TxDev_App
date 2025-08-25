package com.iie.st10089153.txdevsystems_app.ui.dashboard_marene

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentDashboardBinding
import com.iie.st10089153.txdevsystems_app.ui.dashboard_marene.models.GaugeCard
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("DashboardFragment", "onCreateView called") //  Add this
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        //receive imei
        view?.let { super.onViewCreated(it, savedInstanceState) }
        val imei = arguments?.getString("IMEI")
        Log.d("DashboardFragment", "Received IMEI: $imei")


        // Set empty adapter to avoid "No adapter attached" warning
        binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.gaugeRecyclerView.adapter = GaugeAdapter(emptyList())

        // Call API
        loadDashboardItem()

        return binding.root
    }

    private fun loadDashboardItem() {
        lifecycleScope.launch {
            try {
                val imei = "869688057596399" // or dynamically from user/device
                val response = RetrofitClient.instanceDashboard.getDashboardItem(imei)

                // Log the raw response object
                Log.d("DashboardAPI", "Raw response object: $response")

                // Log HTTP status code
                Log.d("DashboardAPI", "HTTP status: ${response.code()}")

                // Log headers
                Log.d("DashboardAPI", "Headers: ${response.headers()}")

                if (response.isSuccessful) {
                    val item = response.body()

                    // Log the response body directly (your parsed data class)
                    Log.d("DashboardAPI", "Parsed response body: $item")

                    // Also log the raw JSON string for debugging
                    val rawJson = response.errorBody()?.string() ?: "No raw error body"
                    Log.d("DashboardAPI", "Raw JSON body (if available): $rawJson")

                    if (item != null) {
                        Log.d("DashboardAPI", "Supply status: ${item.supply_status}")
                        Log.d("DashboardAPI", "Battery voltage: ${item.bat_volt}")
                        Log.d("DashboardAPI", "Temperature now: ${item.temp_now}")
                        Log.d("DashboardAPI", "Door status: ${item.door_status}")

                        val gaugeList = listOf(
                            GaugeCard(
                                iconRes = R.drawable.ic_power,
                                statusText = if (item.supply_status == "Okay") "ON" else "OFF",
                                name = "Power",
                                measurement = null,
                                gaugeImageRes = if (item.supply_status == "Okay") R.drawable.circle_placeholder else R.drawable.circle_placeholder
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_battery_full,
                                statusText = item.bat_volt.toString(),
                                name = "Battery",
                                measurement = "Volts",
                                gaugeImageRes = R.drawable.circle_placeholder
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_temperature,
                                statusText = "${item.temp_now}°C",
                                name = "Temperature",
                                measurement = "Celsius",
                                gaugeImageRes = R.drawable.circle_placeholder
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_door_close,
                                statusText = item.door_status,
                                name = "Door",
                                measurement = null,
                                gaugeImageRes = R.drawable.circle_placeholder
                            )
                        )

                        binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                        binding.gaugeRecyclerView.adapter = GaugeAdapter(gaugeList)
                    } else {
                        Log.e("DashboardAPI", "Response body is null")
                    }
                } else {
                    // Print detailed error response
                    val errorBody = response.errorBody()?.string()
                    Log.e("DashboardAPI", "Error code: ${response.code()}")
                    Log.e("DashboardAPI", "Error body: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("DashboardAPI", "Exception occurred: ${e.localizedMessage}", e)
            }
        }

    }
}

