package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.GridLayoutManager
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentDashboardBinding
import com.iie.st10089153.txdevsystems_app.network.Api.CurrentRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.GaugeCard
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var currentImei: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("DashboardFragment", "onCreateView called")
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        currentImei = arguments?.getString("IMEI")
        Log.d("DashboardFragment", "Received IMEI: $currentImei")

        binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.gaugeRecyclerView.adapter = GaugeAdapter(emptyList())

        currentImei?.let { loadDashboardItem(it) } ?: Log.e("DashboardFragment", "IMEI not provided!")

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dashboard_settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_device_settings -> {
                currentImei?.let { imei ->
                    val bundle = Bundle().apply { putString("IMEI", imei) }
                    findNavController().navigate(R.id.action_dashboard_to_device_settings, bundle)
                } ?: run {
                    Log.e("DashboardFragment", "Cannot open Device Settings: IMEI is null")
                }
                true
            }
            R.id.action_view_charts -> {
                showChartsMenu()
                true
            }
            R.id.action_view_reports -> {
                navigateToReports()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navigateToReports() {
        currentImei?.let { imei ->
            // Get device name from navigation arguments if available
            val deviceName = arguments?.getString("name") ?: "Device"
            Log.d("DashboardFragment", "Navigating to reports with IMEI: $imei, Name: $deviceName")
            findNavController().navigate(
                R.id.action_dashboard_to_reports,
                bundleOf("IMEI" to imei, "name" to deviceName)
            )
        } ?: run {
            Log.e("DashboardFragment", "Cannot navigate to reports: IMEI is null")
        }
    }

    private fun showChartsMenu() {
        val anchor = requireView()
        val popup = androidx.appcompat.widget.PopupMenu(requireContext(), anchor)
        popup.menuInflater.inflate(R.menu.charts_menu, popup.menu)
        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_temperature_chart -> { navigateToChart(R.id.navigation_temperature_chart); true }
                R.id.action_door_chart        -> { navigateToChart(R.id.navigation_door_history_chart); true }
                R.id.action_battery_chart     -> { navigateToChart(R.id.navigation_battery_chart); true }
                else -> false
            }
        }
        popup.show()
    }

    private fun navigateToChart(chartDestination: Int) {
        currentImei?.let { imei ->
            val bundle = Bundle().apply { putString("IMEI", imei) }
            findNavController().navigate(chartDestination, bundle)
        } ?: Log.e("DashboardFragment", "Cannot navigate to chart: IMEI is null")
    }

    private fun loadDashboardItem(imei: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getDashboardApi(requireContext())
                    .getCurrent(CurrentRequest(imei))
                if (response.isSuccessful) {
                    val item = response.body() ?: return@launch
                    val gaugeList = listOf(
                        GaugeCard(
                            iconRes = R.drawable.ic_power,
                            statusText = if (item.supply_status == "Okay") "ON" else "OFF",
                            name = "Power",
                            gaugeImageRes = if (item.supply_status == "Okay") R.drawable.power_on else R.drawable.power_off,
                            type = "" // no gauge
                        ),
                        GaugeCard(
                            iconRes = R.drawable.ic_battery_full,
                            statusText = item.bat_volt.toString(),
                            name = "Battery",
                            measurement = "Volts",
                            minValue = 0f,
                            maxValue = 15f,
                            gaugeImageRes = R.drawable.circle_placeholder,
                            type = "battery" // battery gauge
                        ),
                        GaugeCard(
                            iconRes = R.drawable.ic_temperature,
                            statusText = "${item.temp_now}",
                            name = "Temperature",
                            measurement = "°C",
                            gaugeImageRes = R.drawable.circle_placeholder,
                            minValue = item.temp_min.toFloat(),
                            maxValue = item.temp_max.toFloat(),
                            type = "temperature" // temperature gauge
                        ),
                        GaugeCard(
                            iconRes = R.drawable.ic_door_close,
                            statusText = item.door_status,
                            name = "Door",
                            gaugeImageRes = if (item.door_status_bool == 0) R.drawable.door_closed else R.drawable.door_open,
                            type = "" // no gauge
                        )
                    )

                    binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
                    binding.gaugeRecyclerView.adapter = GaugeAdapter(gaugeList)
                } else {
                    Log.e("DashboardAPI", "Error: ${response.code()} - ${response.errorBody()?.string()}")
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