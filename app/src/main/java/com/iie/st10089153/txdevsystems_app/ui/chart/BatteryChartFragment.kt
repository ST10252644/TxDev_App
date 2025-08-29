package com.iie.st10089153.txdevsystems_app.ui.chart

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.iie.st10089153.txdevsystems_app.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class BatteryChartFragment : Fragment() {
    private val vm: BatteryChartViewModel by viewModels()
    private lateinit var chart: LineChart
    private lateinit var datePill: TextView
    private lateinit var title: TextView
    private lateinit var toggle: MaterialButtonToggleGroup
    private lateinit var rangeLabel: TextView

    private fun argImeiOrFallback(): String {
        val raw = arguments?.getString("IMEI")
            ?: arguments?.getString("imei")
            ?: arguments?.getString("device_imei")
            ?: arguments?.getString("selectedImei")
        return raw?.trim().takeUnless { it.isNullOrBlank() } ?: "869688057596399"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_chart_battery, container, false)
        chart = v.findViewById(R.id.lineChartBattery)
        datePill = v.findViewById(R.id.btnSelectDateBattery)
        title = v.findViewById(R.id.tvScreenTitle)
        toggle = v.findViewById(R.id.toggleRange)
        rangeLabel = v.findViewById(R.id.tvRangeLabel)
        chart.defaultStyle()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imei = argImeiOrFallback()
        title.text = getString(R.string.battery_history_title_fmt, imei)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { ui ->
                    ui.error?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show() }
                    if (ui.points.isNotEmpty()) bindChart(ui.points)
                    datePill.text = ui.day.toString()
                    if (ui.window == RangeWindow.CUSTOM && ui.startIso != null && ui.stopIso != null) {
                        rangeLabel.text = "${prettyIsoDate(ui.startIso)}  →  ${prettyIsoDate(ui.stopIso)}"
                    } else if (rangeLabel.text.isNullOrBlank()) {
                        rangeLabel.text = "—"
                    }
                    styleToggle()
                }
            }
        }

        vm.fetch(requireContext(), imei, LocalDate.now(), RangeWindow.MONTH)
        toggle.check(R.id.btnMonth)
        styleToggle()

        toggle.addOnButtonCheckedListener { _, id, checked ->
            if (!checked) return@addOnButtonCheckedListener
            val win = when (id) {
                R.id.btnDay -> RangeWindow.DAY
                R.id.btnWeek -> RangeWindow.WEEK
                else -> RangeWindow.MONTH
            }
            vm.fetch(requireContext(), imei, vm.ui.value.day, win)
            rangeLabel.text = "—"
            styleToggle()
        }

        datePill.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select date range")
                .build()
            picker.addOnPositiveButtonClickListener { sel ->
                val start = isoStartOfDay(sel.first!!)
                val stop  = isoEndOfDay(sel.second!!)
                vm.fetchRange(requireContext(), imei, start, stop)
                rangeLabel.text = "${prettyIsoDate(start)}  →  ${prettyIsoDate(stop)}"
                toggle.clearChecked()
                styleToggle()
            }
            picker.show(parentFragmentManager, "range_battery")
        }
    }

    private fun bindChart(points: List<com.iie.st10089153.txdevsystems_app.network.Api.RangePoint>) {
        val labels = dateLabels(points)
        val bat = points.mapIndexed { i, p -> Entry(i.toFloat(), p.bat_volt.toFloatOrNaN()) }
        val supply = points.mapIndexed { i, p -> Entry(i.toFloat(), p.supply_volt.toFloatOrNaN()) }

        val purple = ContextCompat.getColor(requireContext(), R.color.battery_purple)
        val green  = ContextCompat.getColor(requireContext(), R.color.supply_green)

        chart.data = LineData(
            ds(bat,   "Battery Volt (V)", purple),
            ds(supply,"Supply Volt (V)",  green)
        )

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelCount = labels.size.coerceAtMost(8)

        val white = ContextCompat.getColor(requireContext(), R.color.white)
        chart.xAxis.textColor = white
        chart.axisLeft.textColor = white

        chart.invalidate()
    }

    /** make selected Day/Week/Month appear green */
    private fun styleToggle() {
        val green = ContextCompat.getColor(requireContext(), R.color.tx_green)
        val transparent = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        val white = ContextCompat.getColor(requireContext(), android.R.color.white)
        val black = ContextCompat.getColor(requireContext(), android.R.color.black)

        val btns = listOf(
            requireView().findViewById<MaterialButton>(R.id.btnDay),
            requireView().findViewById<MaterialButton>(R.id.btnWeek),
            requireView().findViewById<MaterialButton>(R.id.btnMonth)
        )
        btns.forEach { btn ->
            val checked = (toggle.checkedButtonId == btn.id)
            btn.backgroundTintList = ColorStateList.valueOf(if (checked) green else transparent)
            btn.setTextColor(if (checked) black else white)
        }
    }
}
