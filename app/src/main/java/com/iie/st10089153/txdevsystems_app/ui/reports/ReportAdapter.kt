package com.iie.st10089153.txdevsystems_app.ui.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R

class ReportAdapter(private val items: List<ColdRoomData>) :
    RecyclerView.Adapter<ReportAdapter.ReportViewHolder>() {

    inner class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceId: TextView = view.findViewById(R.id.tvDeviceId)
        val tempNow: TextView = view.findViewById(R.id.tvTempNow)
        val doorStatus: TextView = view.findViewById(R.id.tvDoorStatus)
        val timestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val item = items[position]
        holder.deviceId.text = item.deviceid
        holder.tempNow.text = "${item.temp_now}°C"
        holder.doorStatus.text = item.door_status ?: "-"
        holder.timestamp.text = item.timestamp
    }

    override fun getItemCount() = items.size
}