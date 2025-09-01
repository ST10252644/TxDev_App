package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.GaugeCard
import com.iie.st10089153.txdevsystems_app.ui.dashboard.views.BatteryGaugeView
import com.iie.st10089153.txdevsystems_app.ui.dashboard.views.GaugeBackgroundView

class GaugeAdapter(private val items: List<GaugeCard>) :
    RecyclerView.Adapter<GaugeAdapter.GaugeViewHolder>() {

    class GaugeViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val iconView: ImageView = view.findViewById(R.id.icon_view)
        val statusText: TextView = view.findViewById(R.id.status_text)
        val nameText: TextView = view.findViewById(R.id.name_text)
        val measurementText: TextView = view.findViewById(R.id.measurement_text)
        val gaugeContainer: FrameLayout = view.findViewById(R.id.gauge_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GaugeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_gauge_card, parent, false)
        return GaugeViewHolder(view)
    }

    @SuppressLint("MissingInflatedId")
    override fun onBindViewHolder(holder: GaugeViewHolder, position: Int) {
        val item = items[position]

        // Set texts and icon
        holder.iconView.setImageResource(item.iconRes)
        holder.statusText.text = item.statusText
        holder.nameText.text = item.name

        // Clear any previous views in the container
        holder.gaugeContainer.removeAllViews()

        if (item.minValue != null && item.maxValue != null) {
            // Decide which gauge type to inflate
            val gaugeView = if (item.type == "battery") {
                LayoutInflater.from(holder.view.context)
                    .inflate(R.layout.battery_gauge, holder.gaugeContainer, false)
            } else {
                LayoutInflater.from(holder.view.context)
                    .inflate(R.layout.temp_gauge, holder.gaugeContainer, false)
            }

            holder.gaugeContainer.addView(gaugeView)

            // Animate the correct gauge
            if (item.type == "battery") {
                val batteryGauge = gaugeView.findViewById<BatteryGaugeView>(R.id.batteryGaugeView)
                batteryGauge.animateToValue(item.statusText?.toFloatOrNull() ?: item.minValue.toFloat(), 1000)
                holder.measurementText.text = "Volts"
            } else {
                val tempGauge = gaugeView.findViewById<GaugeBackgroundView>(R.id.gaugeBackgroundView)
                tempGauge.updateRanges(item.minValue.toFloat(), item.maxValue.toFloat())
                tempGauge.animateToValue(item.statusText?.toFloatOrNull() ?: item.minValue.toFloat(), 1000)
                holder.measurementText.text = "°C"
            }

            holder.measurementText.visibility = View.VISIBLE

        } else {
            // Fallback: show image if gauge not available
            val imageView = ImageView(holder.view.context).apply {
                setImageResource(item.gaugeImageRes)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            holder.gaugeContainer.addView(imageView)
            holder.measurementText.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = items.size
}
