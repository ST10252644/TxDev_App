package com.iie.st10089153.txdevsystems_app.ui.chart

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.iie.st10089153.txdevsystems_app.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate

class DoorChartFragment : Fragment() {

    private val vm: DoorChartViewModel by viewModels()
    private lateinit var chart: LineChart
    private lateinit var datePill: TextView
    private lateinit var title: TextView
    private lateinit var toggle: MaterialButtonToggleGroup
    private lateinit var rangeLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_chart_open_door, container, false)
        chart = v.findViewById(R.id.lineChartDoor)
        datePill = v.findViewById(R.id.btnSelectDateDoor)
        title = v.findViewById(R.id.tvScreenTitle)
        toggle = v.findViewById(R.id.toggleRange)
        rangeLabel = v.findViewById(R.id.tvRangeLabel)

        chart.defaultStyle()
        chart.axisLeft.axisMinimum = -0.1f
        chart.axisLeft.axisMaximum = 1.1f
        chart.axisLeft.granularity = 1f
        chart.axisLeft.valueFormatter = DoorStateFormatter()
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val imei = resolveImeiFlexible()
        if (imei.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Device IMEI not provided", Toast.LENGTH_LONG).show()
            title.text = getString(R.string.open_door_history_title_fmt, "Unknown Device")
            return
        }

        title.text = getString(R.string.open_door_history_title_fmt, imei)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.ui.collectLatest { ui ->
                    ui.error?.let {
                        Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                    }
                    if (ui.points.isNotEmpty()) bindChart(ui.points)

                    // Update date pill text based on window type
                    if (ui.window == RangeWindow.CUSTOM && ui.startIso != null && ui.stopIso != null) {
                        datePill.text = "${prettyIsoDate(ui.startIso)} → ${prettyIsoDate(ui.stopIso)}"
                        rangeLabel.text = "" // Hide range label when custom date is shown in pill
                    } else {
                        datePill.text = ui.day.toString()
                        if (rangeLabel.text.isNullOrBlank()) {
                            rangeLabel.text = ""
                        }
                    }
                    styleToggle()
                }
            }
        }

        // Initial load
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

        // Create date picker functionality
        val datePickerFunction = {
            val picker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select date range")
                .build()
            picker.addOnPositiveButtonClickListener { selection ->
                val start = isoStartOfDay(selection.first!!)
                val stop = isoEndOfDay(selection.second!!)
                vm.fetchRange(requireContext(), imei, start, stop)

                toggle.clearChecked()
                styleToggle()
            }
            picker.show(parentFragmentManager, "range_door")
        }

        // Add click listeners to both text and calendar icon
        datePill.setOnClickListener { datePickerFunction() }
        view.findViewById<ImageView>(R.id.ivCalendarBattery3).setOnClickListener { datePickerFunction() }
    }

    private fun bindChart(points: List<com.iie.st10089153.txdevsystems_app.network.Api.RangePoint>) {
        val labels = dateLabels(points)
        val door = points.mapIndexed { i, p ->
            Entry(i.toFloat(), (p.door_status_bool?.toFloatOrNull() ?: 0f))
        }

        val purple = ContextCompat.getColor(requireContext(), R.color.door_status_purple)
        chart.data = LineData(ds(door, "Door Status", purple))

        // Safe ValueFormatter for x-axis
        chart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val i = value.toInt()
                return if (i in labels.indices) labels[i] else ""
            }
        }

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