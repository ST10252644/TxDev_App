package com.iie.st10089153.txdevsystems_app.ui.chart

import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
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
    xAxis.setDrawGridLines(false)
}

private val FMT_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

private fun parseLocal(ts: String): LocalDateTime {
    runCatching { return Instant.parse(ts).atZone(ZoneId.systemDefault()).toLocalDateTime() }
    runCatching { return OffsetDateTime.parse(ts).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime() }
    return LocalDateTime.parse(ts, FMT_LOCAL)
}

fun timeLabels(points: List<RangePoint>) = points.map { p -> parseLocal(p.timestamp).toLocalTime().toString() }

fun ds(values: List<Entry>, label: String) = LineDataSet(values, label).apply {
    lineWidth = 2f
    setDrawCircles(false)
    mode = LineDataSet.Mode.CUBIC_BEZIER
}

fun Any?.toFloatOrNaN(): Float = when (this) {
    null -> Float.NaN
    is Number -> this.toFloat()
    else -> this.toString().trim().replace("+", "").toFloatOrNull() ?: Float.NaN
}
