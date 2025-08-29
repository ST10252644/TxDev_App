package com.iie.st10089153.txdevsystems_app.ui.chart

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
import com.google.android.material.datepicker.MaterialDatePicker
import com.iie.st10089153.txdevsystems_app.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class TempChartFragment : Fragment() {
    private val vm: TempChartViewModel by viewModels()
    private lateinit var chart: LineChart
    private lateinit var datePill: TextView
    private lateinit var title: TextView
    private lateinit var rangeLabel: TextView

    private fun readImeiArg(): String? =
        arguments?.getString("IMEI")
            ?: arguments?.getString("imei")
            ?: arguments?.getString("device_imei")
            ?: arguments?.getString("selectedImei")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_chart_temperature, container, false)
        chart = v.findViewById(R.id.lineChartTemp)
        datePill = v.findViewById(R.id.btnSelectDateTemp)
        title = v.findViewById(R.id.tvScreenTitle)
        rangeLabel = v.findViewById(R.id.tvRangeLabel)
        chart.defaultStyle()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imei = readImeiArg() ?: "869688057596399"
        title.text = getString(R.string.temperature_chart_title_fmt, imei)

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
                }
            }
        }

        vm.fetch(requireContext(), imei, LocalDate.now(), RangeWindow.MONTH)

        datePill.setOnClickListener {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select date range")
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                val start = isoStartOfDay(selection.first!!)
                val stop  = isoEndOfDay(selection.second!!)
                vm.fetchRange(requireContext(), imei, start, stop)
                rangeLabel.text = "${prettyIsoDate(start)}  →  ${prettyIsoDate(stop)}"
            }
            picker.show(parentFragmentManager, "range_temp")
        }
    }

    private fun bindChart(points: List<com.iie.st10089153.txdevsystems_app.network.Api.RangePoint>) {
        val labels = dateLabels(points)
        val minE = points.mapIndexed { i, p -> Entry(i.toFloat(), p.temp_min.toFloatOrNaN()) }
        val maxE = points.mapIndexed { i, p -> Entry(i.toFloat(), p.temp_max.toFloatOrNaN()) }
        val nowE = points.mapIndexed { i, p -> Entry(i.toFloat(), p.temp_now.toFloatOrNaN()) }

        val green  = ContextCompat.getColor(requireContext(), R.color.min_temp_green)
        val red    = ContextCompat.getColor(requireContext(), R.color.max_temp_red)
        val purple = ContextCompat.getColor(requireContext(), R.color.actual_temp_purple)

        chart.data = LineData(
            ds(minE, "Min Set Temp", green),
            ds(maxE, "Max Set Temp", red),
            ds(nowE, "Actual Temp", purple)
        )

        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.labelCount = labels.size.coerceAtMost(8)

        val white = ContextCompat.getColor(requireContext(), R.color.white)
        chart.xAxis.textColor = white
        chart.axisLeft.textColor = white

        chart.invalidate()
    }
}
