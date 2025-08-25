package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableRow
import android.widget.TextView
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

        // receive IMEI
        view?.let { super.onViewCreated(it, savedInstanceState) }
        val imei = arguments?.getString("IMEI")
        Log.d("DashboardFragment", "Received IMEI: $imei")

        // Set empty adapter to avoid "No adapter attached" warning
        binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.gaugeRecyclerView.adapter = GaugeAdapter(emptyList())

        // Call Dashboard API
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
                        // Map response to your gauges
                        val gaugeList = listOf(
                            GaugeCard(
                                iconRes = R.drawable.ic_power,
                                statusText = if (item.supply_status == "Okay") "ON" else "OFF",
                                name = "Power",
                                gaugeImageRes = R.drawable.circle_placeholder
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
                                gaugeImageRes = R.drawable.circle_placeholder
                            )
                        )

                        binding.gaugeRecyclerView.layoutManager =
                            GridLayoutManager(requireContext(), 2)

                        binding.gaugeRecyclerView.adapter = GaugeAdapter(gaugeList)


                        //  Populate the table at the bottom
                        populateDashboardTable(item)

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

    private fun populateDashboardTable(item: com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem) {
        binding.dashboardTable.removeAllViews() // clear previous rows

        fun addRow(label: String, value: String) {
            val tableRow = TableRow(requireContext())
            val tvLabel = TextView(requireContext()).apply {
                text = label
                setPadding(8, 8, 8, 8)
            }
            val tvValue = TextView(requireContext()).apply {
                text = value
                setPadding(8, 8, 8, 8)
            }
            tableRow.addView(tvLabel)
            tableRow.addView(tvValue)
            binding.dashboardTable.addView(tableRow)
        }

        // Add all fields
        item.run {
            addRow("ID", id.toString())
            addRow("IMEI", imei)
            addRow("Temp Max", temp_max.toString())
            addRow("Temp Now", temp_now.toString())
            addRow("Temp Min", temp_min.toString())
            addRow("Supply Volt", supply_volt.toString())
            addRow("Battery Volt", bat_volt.toString())
            addRow("Supply Status", supply_status)
            addRow("Battery Status", bat_status)
            addRow("Door Status", door_status)
            addRow("Door Status Bool", door_status_bool.toString())
            addRow("Signal Strength", signal_strength)
            addRow("Timestamp", timestamp)
            addRow("Active", active.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
