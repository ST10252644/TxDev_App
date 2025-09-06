package com.iie.st10089153.txdevsystems_app.ui.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R

class ReportExtendedAdapter(private var items: List<ColdRoomData>) :
    RecyclerView.Adapter<ReportExtendedAdapter.VH>() {

    fun submit(newItems: List<ColdRoomData>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDeviceId: TextView = v.findViewById(R.id.tvDeviceId)
        val tvTempNow: TextView = v.findViewById(R.id.tvTempNow)
        val tvMinTemp: TextView = v.findViewById(R.id.tvMinTemp)
        val tvMaxTemp: TextView = v.findViewById(R.id.tvMaxTemp)
        val tvSupply: TextView = v.findViewById(R.id.tvSupply)
        val tvBattery: TextView = v.findViewById(R.id.tvBattery)
        val tvDoor: TextView = v.findViewById(R.id.tvDoorStatus)
        val tvTimestamp: TextView = v.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_report_extended, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = items[pos]
        h.tvDeviceId.text = it.deviceid
        h.tvTempNow.text = "${it.temp_now}°C"
        h.tvMinTemp.text = it.min_temp?.toString() ?: "-"
        h.tvMaxTemp.text = it.max_temp?.toString() ?: "-"
        h.tvSupply.text = it.supply_status ?: "-"
        h.tvBattery.text = it.batt_status ?: "-"
        h.tvDoor.text = it.door_status ?: "-"
        h.tvTimestamp.text = it.timestamp
    }

    override fun getItemCount() = items.size
}
