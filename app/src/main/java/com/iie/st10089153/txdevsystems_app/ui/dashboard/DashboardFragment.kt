package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View
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
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.core.content.ContextCompat

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private var currentImei: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        /*setHasOptionsMenu(true)*/
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("DashboardFragment", "onCreateView called")
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        currentImei = arguments?.getString("IMEI")
        Log.d("DashboardFragment", "Received IMEI: $currentImei")

        setupRecyclerView()
        setupSwipeRefresh()
        setupClickListeners()

        currentImei?.let { loadDashboardItem(it) }
            ?: run { showErrorState("IMEI not provided!") }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.gaugeRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.gaugeRecyclerView.adapter = GaugeAdapter(emptyList())
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            currentImei?.let { loadDashboardItem(it) }
                ?: run { binding.swipeRefresh.isRefreshing = false }
        }
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            currentImei?.let { loadDashboardItem(it) }
        }
    }

    private fun showEmptyState() {
        binding.gaugeRecyclerView.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
    }

    private fun showErrorState(message: String) {
        binding.gaugeRecyclerView.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.VISIBLE
        binding.tvErrorMessage.text = message
    }

    private fun showDashboard() {
        binding.gaugeRecyclerView.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
    }

    private fun loadDashboardItem(imei: String) {
        // Show progress bar only if list is currently empty
        binding.progressBar.visibility =
            if ((binding.gaugeRecyclerView.adapter as GaugeAdapter).itemCount == 0) View.VISIBLE
            else View.GONE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.getDashboardApi(requireContext())
                    .getCurrent(CurrentRequest(imei))

                if (response.isSuccessful) {
                    val item = response.body()
                    if (item == null) {
                        showEmptyState()
                    } else {
                        val gaugeList = listOf(
                            GaugeCard(
                                iconRes = R.drawable.ic_power,
                                statusText = if (item.supply_status == "Okay") "ON" else "OFF",
                                name = "Power",
                                gaugeImageRes = if (item.supply_status == "Okay") R.drawable.power_on else R.drawable.power_off,
                                type = ""
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_battery_full,
                                statusText = item.bat_volt.toString(),
                                name = "Battery",
                                measurement = "Volts",
                                minValue = 0f,
                                maxValue = 15f,
                                gaugeImageRes = R.drawable.circle_placeholder,
                                type = "battery"
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_temperature,
                                statusText = "${item.temp_now}",
                                name = "Temperature",
                                measurement = "°C",
                                gaugeImageRes = R.drawable.circle_placeholder,
                                minValue = item.temp_min.toFloat(),
                                maxValue = item.temp_max.toFloat(),
                                type = "temperature"
                            ),
                            GaugeCard(
                                iconRes = R.drawable.ic_door_close,
                                statusText = item.door_status,
                                name = "Door",
                                gaugeImageRes = if (item.door_status_bool == 0) R.drawable.door_closed else R.drawable.door_open,
                                type = ""
                            )
                        )

                        binding.gaugeRecyclerView.adapter = GaugeAdapter(gaugeList)
                        showDashboard()
                    }
                } else {
                    showErrorState("Error: ${response.code()}")
                    Log.e("DashboardAPI", "Error: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                showErrorState("Failed to load: ${e.localizedMessage}")
                Log.e("DashboardAPI", "Exception occurred: ${e.localizedMessage}", e)
            } finally {
                binding.progressBar.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_device_settings -> {
                currentImei?.let { imei ->
                    val bundle = Bundle().apply { putString("IMEI", imei) }
                    findNavController().navigate(R.id.action_dashboard_to_device_settings, bundle)
                } ?: run { showErrorState("Cannot open Device Settings: IMEI is null") }
                true
            }
            R.id.action_view_charts -> {
                showCustomChartsMenu()
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
            val deviceName = arguments?.getString("name") ?: "Device"
            findNavController().navigate(
                R.id.action_dashboard_to_reports,
                bundleOf("IMEI" to imei, "name" to deviceName)
            )
        } ?: showErrorState("Cannot navigate to reports: IMEI is null")
    }

    // ✅ Updated to use custom styled popup menu
    private fun showCustomChartsMenu() {
        val inflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.custom_charts_menu_layout, null)

        val popupWindow = PopupWindow(
            popupView,
            247.dpToPx(),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.popup_menu_background))
        popupWindow.elevation = 8f

        // Find menu items and set click listeners
        val tempChart = popupView.findViewById<View>(R.id.menu_temperature_chart)
        val doorChart = popupView.findViewById<View>(R.id.menu_door_chart)
        val batteryChart = popupView.findViewById<View>(R.id.menu_battery_chart)

        tempChart.setOnClickListener {
            navigateToChart(R.id.navigation_temperature_chart)
            popupWindow.dismiss()
        }

        doorChart.setOnClickListener {
            navigateToChart(R.id.navigation_door_history_chart)
            popupWindow.dismiss()
        }

        batteryChart.setOnClickListener {
            navigateToChart(R.id.navigation_battery_chart)
            popupWindow.dismiss()
        }

        // Show popup anchored to the view (you might need to adjust positioning)
        val anchorView = requireView()
        popupWindow.showAsDropDown(anchorView, 0, -anchorView.height)
    }

    // Extension function to convert dp to px
    private fun Int.dpToPx(): Int {
        return (this * resources.displayMetrics.density).toInt()
    }

    private fun navigateToChart(chartDestination: Int) {
        currentImei?.let { imei ->
            findNavController().navigate(chartDestination, bundleOf("IMEI" to imei))
        } ?: showErrorState("Cannot navigate to chart: IMEI is null")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}