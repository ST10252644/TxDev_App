package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.GaugeCard
import com.github.anastr.speedviewlib.SpeedView

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

    override fun onBindViewHolder(holder: GaugeViewHolder, position: Int) {
        val item = items[position]

        holder.iconView.setImageResource(item.iconRes)
        holder.statusText.text = item.statusText
        holder.nameText.text = item.name
        holder.measurementText.visibility = item.measurement?.let {
            holder.measurementText.text = it
            View.VISIBLE
        } ?: View.INVISIBLE

        holder.gaugeContainer.removeAllViews()

        // If minValue and maxValue exist, show SpeedView gauge
        if (item.minValue != null && item.maxValue != null) {
            val gaugeView = LayoutInflater.from(holder.view.context)
                .inflate(R.layout.temp_gauge, holder.gaugeContainer, false)
            holder.gaugeContainer.addView(gaugeView)

            val speedGauge = gaugeView.findViewById<SpeedView>(R.id.tempGauge)
            speedGauge.speedometerWidth = 25f
            speedGauge.withTremble = false
            speedGauge.minSpeed = item.minValue
            speedGauge.maxSpeed = item.maxValue
            speedGauge.setStartDegree(135)
            speedGauge.setEndDegree(405)

            val currentVal = item.statusText?.toFloatOrNull() ?: item.minValue
            speedGauge.speedTo(currentVal, 1000)
        } else {
            // No gauge, just show image
            val imageView = ImageView(holder.view.context).apply {
                setImageResource(item.gaugeImageRes)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                scaleType = ImageView.ScaleType.CENTER_INSIDE
            }
            holder.gaugeContainer.addView(imageView)
        }
    }

    override fun getItemCount(): Int = items.size
}
