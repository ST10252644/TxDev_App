package com.iie.st10089153.txdevsystems_app.ui.chart

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun LineChart.defaultStyle() {
    description.isEnabled = false
    legend.isEnabled = false
    setTouchEnabled(true)
    setPinchZoom(true)
    axisRight.isEnabled = false
    axisLeft.setDrawGridLines(true)
    axisLeft.enableGridDashedLine(10f, 8f, 0f)
    xAxis.position = XAxis.XAxisPosition.BOTTOM
    xAxis.setDrawGridLines(true)
    xAxis.granularity = 1f
    xAxis.labelRotationAngle = -90f   // vertical date labels
    xAxis.setAvoidFirstLastClipping(true)
}

private val FMT_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
private val FMT_DATE  = DateTimeFormatter.ofPattern("dd-MM-yyyy")

private fun parseLocal(ts: String): LocalDateTime {
    runCatching { return Instant.parse(ts).atZone(ZoneId.systemDefault()).toLocalDateTime() }
    runCatching { return OffsetDateTime.parse(ts).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }
    return LocalDateTime.parse(ts, FMT_LOCAL)
}

/** X-axis labels as dd-MM-yyyy for all charts */
fun dateLabels(points: List<RangePoint>) = points.map { p ->
    parseLocal(p.timestamp).toLocalDate().format(FMT_DATE)
}

/** Smooth line dataset, no circles */
fun ds(values: List<Entry>, label: String) = LineDataSet(values, label).apply {
    lineWidth = 2f
    setDrawCircles(false)
    mode = LineDataSet.Mode.CUBIC_BEZIER
    setDrawValues(false)
}

/** Same as above but with a fixed color (to match legend) */
fun ds(values: List<Entry>, label: String, color: Int) = LineDataSet(values, label).apply {
    lineWidth = 2f
    setDrawCircles(false)
    mode = LineDataSet.Mode.CUBIC_BEZIER
    setDrawValues(false)
    this.color = color
}

/** Safe float conversion for strings like "+17.2" */
fun Any?.toFloatOrNaN(): Float = when (this) {
    null -> Float.NaN
    is Number -> this.toFloat()
    else -> this.toString().trim().replace("+", "").toFloatOrNull() ?: Float.NaN
}

/** Door Y-axis: show Open / Closed instead of 1 / 0 */
class DoorStateFormatter : ValueFormatter() {
    override fun getFormattedValue(value: Float): String = if (value >= 0.5f) "Open" else "Closed"
}
