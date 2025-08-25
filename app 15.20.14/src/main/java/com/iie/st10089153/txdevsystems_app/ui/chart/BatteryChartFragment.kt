package com.iie.st10089153.txdevsystems_app.ui.chart

import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.iie.st10089153.txdevsystems_app.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class BatteryChartFragment : Fragment() {

    private val vm: BatteryChartViewModel by viewModels()
    private lateinit var chart: LineChart
    private lateinit var datePill: TextView
    private lateinit var title: TextView
    private lateinit var toggle: MaterialButtonToggleGroup



    // Add this helper in each fragment (or put it in a shared util file)
    private fun argImeiOrFallback(): String {
        val raw = arguments?.getString("IMEI")
            ?: arguments?.getString("imei")
            ?: arguments?.getString("device_imei")
            ?: arguments?.getString("selectedImei")
        return raw?.trim().takeUnless { it.isNullOrBlank() } ?: "869688057596399" // fallback
    }



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_chart_battery, container, false)
        chart = v.findViewById(R.id.lineChartBattery)
        datePill = v.findViewById(R.id.btnSelectDateBattery)
        title = v.findViewById(R.id.tvScreenTitle)
        toggle = v.findViewById(R.id.toggleRange)
        chart.defaultStyle()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imei = argImeiOrFallback() // ← TEMP fallback
        title.text = getString(R.string.battery_history_title_fmt, imei)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { ui ->
                    ui.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
                    if (ui.points.isNotEmpty()) bindChart(ui.points)
                    datePill.text = ui.day.toString()
                }
            }
        }

        vm.fetch(requireContext(), imei, LocalDate.now(), RangeWindow.MONTH)

        toggle.addOnButtonCheckedListener { _, id, checked ->
            if (!checked) return@addOnButtonCheckedListener
            val win = when (id) {
                R.id.btnDay -> RangeWindow.DAY
                R.id.btnWeek -> RangeWindow.WEEK
                else -> RangeWindow.MONTH
            }
            vm.fetch(requireContext(), imei, vm.ui.value.day, win)
        }

        datePill.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").build()
            picker.addOnPositiveButtonClickListener { millis ->
                val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                vm.fetch(requireContext(), imei, picked, vm.ui.value.window)
            }
            picker.show(parentFragmentManager, "date")
        }
    }

    private fun bindChart(points: List<com.iie.st10089153.txdevsystems_app.network.Api.RangePoint>) {
        val labels = timeLabels(points)
        val bat = points.mapIndexed { i, p -> Entry(i.toFloat(), p.bat_volt.toFloatOrNaN()) }
        val supply = points.mapIndexed { i, p -> Entry(i.toFloat(), p.supply_volt.toFloatOrNaN()) }
        chart.data = LineData(ds(bat, "Battery Volt (V)"), ds(supply, "Supply Volt (V)"))
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.invalidate()
    }
}
