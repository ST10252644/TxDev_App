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
import com.google.android.material.datepicker.MaterialDatePicker
import com.iie.st10089153.txdevsystems_app.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DoorChartFragment : Fragment() {

    private val vm: DoorChartViewModel by viewModels()
    private lateinit var chart: LineChart
    private lateinit var datePill: TextView
    private lateinit var title: TextView

    private fun readImeiArg(): String? =
        arguments?.getString("IMEI")
            ?: arguments?.getString("imei")
            ?: arguments?.getString("device_imei")
            ?: arguments?.getString("selectedImei")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val v = inflater.inflate(R.layout.fragment_chart_open_door, container, false)
        chart = v.findViewById(R.id.lineChartDoor)
        datePill = v.findViewById(R.id.btnSelectDateDoor)
        title = v.findViewById(R.id.tvScreenTitle)
        chart.defaultStyle()
        chart.axisLeft.axisMinimum = -0.1f
        chart.axisLeft.axisMaximum = 1.1f
        chart.axisLeft.granularity = 1f
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imei = readImeiArg() ?: "869688057596399"   // ← TEMP fallback
        title.text = getString(R.string.open_door_history_title_fmt, imei)

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
        val open = points.mapIndexed { i, p -> Entry(i.toFloat(), (p.door_status_bool?.toFloatOrNull() ?: 0f)) }
        chart.data = LineData(ds(open, "Door Status"))
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.invalidate()
    }
}
