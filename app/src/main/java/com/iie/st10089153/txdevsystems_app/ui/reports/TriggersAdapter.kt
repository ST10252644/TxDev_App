package com.iie.st10089153.txdevsystems_app.ui.reports

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R

class TriggersAdapter(private var items: List<ColdRoomTriggerRow>) :
    RecyclerView.Adapter<TriggersAdapter.VH>() {

    fun submit(newItems: List<ColdRoomTriggerRow>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val tvDeviceId: TextView = v.findViewById(R.id.tvDeviceId)
        val tvTempNow: TextView = v.findViewById(R.id.tvTempNow)
        val tvMinTemp: TextView = v.findViewById(R.id.tvMinTemp)
        val tvMaxTemp: TextView = v.findViewById(R.id.tvMaxTemp)
        val tvTimestamp: TextView = v.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_trigger_row, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        val it = items[pos]
        h.tvDeviceId.text = it.deviceid
        h.tvTempNow.text  = it.temp_now?.let { t -> "$t°C" } ?: "-"
        h.tvMinTemp.text  = it.min_temp?.toString() ?: "-"
        h.tvMaxTemp.text  = it.max_temp?.toString() ?: "-"
        h.tvTimestamp.text = it.timestamp
    }

    override fun getItemCount() = items.size
}
