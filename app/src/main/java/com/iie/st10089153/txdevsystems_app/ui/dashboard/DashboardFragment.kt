package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.ui.dashboard.GaugeAdapter
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.GaugeCard
import com.iie.st10089153.txdevsystems_app.databinding.FragmentDashboardBinding
import com.iie.st10089153.txdevsystems_app.network.Api.CurrentRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("DashboardFragment", "onCreateView called")
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        val imei = arguments?.getString("IMEI")
        Log.d("DashboardFragment", "Received IMEI: $imei")

        binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.gaugeRecyclerView.adapter = GaugeAdapter(emptyList())

        if (!imei.isNullOrEmpty()) {
            loadDashboardItem(imei)
        } else {
            Log.e("DashboardFragment", "IMEI not provided!")
        }

        return binding.root
    }

    private fun loadDashboardItem(imei: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getDashboardApi(requireContext()).getCurrent(
                    CurrentRequest(imei)
                )

                if (response.isSuccessful) {
                    val item = response.body()
                    if (item != null) {
                        val gaugeList = listOf(
                            GaugeCard(
                                iconRes = R.drawable.ic_power,
                                statusText = if (item.supply_status == "Okay") "ON" else "OFF",
                                name = "Power",
                                gaugeImageRes = if (item.supply_status == "Okay") R.drawable.power_on else R.drawable.circle_placeholder
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_battery_full,
                                statusText = item.bat_volt.toString(),
                                name = "Battery",
                                measurement = "Volts",
                                minValue = 0f,
                                maxValue = 15f,
                                gaugeImageRes = R.drawable.circle_placeholder
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_temperature,
                                statusText = "${item.temp_now}",
                                name = "Temperature",
                                measurement = "°C",
                                gaugeImageRes = R.drawable.circle_placeholder,
                                minValue = item.temp_min.toFloat(),
                                maxValue = item.temp_max.toFloat()
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_door_close,
                                statusText = item.door_status,
                                name = "Door",
                                gaugeImageRes = if (item.door_status_bool == 0) R.drawable.door_closed else R.drawable.circle_placeholder
                            )
                        )

                        binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                        binding.gaugeRecyclerView.adapter = GaugeAdapter(gaugeList)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DashboardAPI", "Error: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("DashboardAPI", "Exception occurred: ${e.localizedMessage}", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
