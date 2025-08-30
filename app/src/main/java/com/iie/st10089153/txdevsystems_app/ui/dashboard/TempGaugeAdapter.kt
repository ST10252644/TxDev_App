package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import com.github.anastr.speedviewlib.SpeedView
import com.github.anastr.speedviewlib.components.Section
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.TempGauge

class TempGaugeAdapter(private val items: List<TempGauge>) :
    RecyclerView.Adapter<TempGaugeAdapter.TempGaugeViewHolder>() {

    class TempGaugeViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.icon_view)
        val statusText: TextView = view.findViewById(R.id.status_text)
        val nameText: TextView = view.findViewById(R.id.name_text)
        val measurementText: TextView = view.findViewById(R.id.measurement_text)
        val gaugeContainer: FrameLayout = view.findViewById(R.id.gauge_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TempGaugeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gauge_card, parent, false)
        return TempGaugeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TempGaugeViewHolder, position: Int) {
        val item = items[position]

        holder.iconView.setImageResource(item.iconRes)
        holder.statusText.text = item.statusText
        holder.nameText.text = item.name
        holder.measurementText.visibility = item.measurement?.let {
            holder.measurementText.text = it
            View.VISIBLE
        } ?: View.INVISIBLE

        // Clear previous gauge
        holder.gaugeContainer.removeAllViews()

        val tempGaugeView = LayoutInflater.from(holder.view.context)
            .inflate(R.layout.temp_gauge, holder.gaugeContainer, false)
        holder.gaugeContainer.addView(tempGaugeView)

        val tempGauge = tempGaugeView.findViewById<SpeedView>(R.id.tempGauge)
        fun Float.toPx() = this * holder.view.context.resources.displayMetrics.density
        tempGauge.speedometerWidth = 8f.toPx()

        val minTemp = item.minValue ?: -10f
        val maxTemp = item.maxValue ?: 40f
        val safeMin = item.safeMin?.coerceIn(minTemp, maxTemp) ?: minTemp
        val safeMax = item.safeMax?.coerceIn(minTemp, maxTemp) ?: maxTemp
        val currentTemp = item.statusText?.toFloatOrNull()?.coerceIn(minTemp, maxTemp) ?: minTemp

        tempGauge.withTremble = false
        tempGauge.clearSections()
        tempGauge.setStartDegree(135)
        tempGauge.setEndDegree(405)
        tempGauge.minSpeed = 0f
        tempGauge.maxSpeed = 100f

        val sectionWidth = 12f

        // Map safe zones to 0-100%
        val safeMinPercent = ((safeMin - minTemp) / (maxTemp - minTemp) * 100f).coerceIn(0f, 100f)
        val safeMaxPercent = ((safeMax - minTemp) / (maxTemp - minTemp) * 100f).coerceIn(0f, 100f)

        // Red before safe zone
        if (safeMinPercent > 0f) {
            tempGauge.addSections(Section(0f, safeMinPercent, Color.RED, sectionWidth))
        }
        // Green safe zone
        if (safeMaxPercent > safeMinPercent) {
            tempGauge.addSections(Section(safeMinPercent, safeMaxPercent, Color.GREEN, sectionWidth))
        }
        // Red after safe zone
        if (safeMaxPercent < 100f) {
            tempGauge.addSections(Section(safeMaxPercent, 100f, Color.RED, sectionWidth))
        }

        // Map current temperature to 0-100%
        val gaugeValue = ((currentTemp - minTemp) / (maxTemp - minTemp) * 100f).coerceIn(0f, 100f)
        tempGauge.speedTo(gaugeValue, 1000)
    }

    override fun getItemCount(): Int = items.size
}
