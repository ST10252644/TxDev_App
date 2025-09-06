package com.iie.st10089153.txdevsystems_app.ui.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R

class ReportDataAdapter(private var items: List<ReportItem>) :
    RecyclerView.Adapter<ReportDataAdapter.ReportViewHolder>() {

    fun updateData(newItems: List<ReportItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tempNow: TextView = view.findViewById(R.id.tvTempNow)
        val doorIcon: ImageView = view.findViewById(R.id.ivDoorStatus)
        val powerIcon: ImageView = view.findViewById(R.id.ivPowerStatus)
        val batteryIcon: ImageView = view.findViewById(R.id.ivBatteryStatus)
        val timestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_data, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.tempNow.text = item.tempNow
        holder.timestamp.text = item.timestamp

        // Set door icon and color
        when (item.doorStatus) {
            DoorStatus.OPEN -> {
                holder.doorIcon.setImageResource(R.drawable.ic_door_open)
                holder.doorIcon.setColorFilter(ContextCompat.getColor(context, R.color.red_error))
            }
            DoorStatus.CLOSED -> {
                holder.doorIcon.setImageResource(R.drawable.ic_door_close)
                holder.doorIcon.setColorFilter(ContextCompat.getColor(context, R.color.green_success))
            }
        }

        // Set power icon and color
        when (item.powerStatus) {
            PowerStatus.OK -> {
                holder.powerIcon.setImageResource(R.drawable.ic_power)
                holder.powerIcon.setColorFilter(ContextCompat.getColor(context, R.color.green_success))
            }
            PowerStatus.ERROR -> {
                holder.powerIcon.setImageResource(R.drawable.ic_power_off)
                holder.powerIcon.setColorFilter(ContextCompat.getColor(context, R.color.red_error))
            }
        }

        // Set battery icon and color
        when (item.batteryStatus) {
            BatteryStatus.OK -> {
                holder.batteryIcon.setImageResource(R.drawable.ic_battery_full)
                holder.batteryIcon.setColorFilter(ContextCompat.getColor(context, R.color.green_success))
            }
            BatteryStatus.LOW -> {
                holder.batteryIcon.setImageResource(R.drawable.ic_battery_low)
                holder.batteryIcon.setColorFilter(ContextCompat.getColor(context, R.color.red_error))
            }
            BatteryStatus.ERROR -> {
                holder.batteryIcon.setImageResource(R.drawable.ic_bad)
                holder.batteryIcon.setColorFilter(ContextCompat.getColor(context, R.color.red_error))
            }
        }
    }

    override fun getItemCount() = items.size
}