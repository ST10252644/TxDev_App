package com.iie.st10089153.txdevsystems_app.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.CurrentRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeviceAdapter(private val devices: List<AvailableUnit>) :
    RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val deviceTemp: TextView = view.findViewById(R.id.deviceTemp)
        val deviceStatus: TextView = view.findViewById(R.id.deviceStatus)
        val deviceLastSeen: TextView = view.findViewById(R.id.deviceLastSeen)
        val deviceBattery: ImageView = view.findViewById(R.id.deviceBattery)
        val deviceDoor: ImageView = view.findViewById(R.id.deviceDoor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.device_card, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val unit = devices[position]

        holder.deviceName.text = unit.name
        holder.deviceStatus.text = if (unit.status.equals("Active", ignoreCase = true)) "● Online" else "● Offline"
        holder.deviceLastSeen.text = "Last refreshed: ${unit.last_seen}"

        // Call Dashboard API with coroutines
        (holder.itemView.context as? FragmentActivity)?.lifecycleScope?.launch {
            try {
                val api = RetrofitClient.getDashboardApi(holder.itemView.context)
                val response = withContext(Dispatchers.IO) {
                    api.getCurrent(CurrentRequest(unit.imei))
                }

                if (response.isSuccessful && response.body() != null) {
                    val item = response.body()!!

                    // Temperature
                    holder.deviceTemp.text = "${item.temp_now}"
                    holder.deviceTemp.setTextColor(holder.itemView.context.getColor(R.color.primary))

                    // Battery
                    if (item.bat_status.equals("Okay", ignoreCase = true)) {
                        holder.deviceBattery.setImageResource(R.drawable.ic_battery_full)
                        holder.deviceBattery.setColorFilter(holder.itemView.context.getColor(R.color.primary))
                    } else {
                        holder.deviceBattery.setImageResource(R.drawable.ic_battery_empty)
                        holder.deviceBattery.setColorFilter(holder.itemView.context.getColor(R.color.red_error))
                    }

                    // Door
                    if (item.door_status_bool == 0) {
                        holder.deviceDoor.setImageResource(R.drawable.ic_door_close)
                        holder.deviceDoor.setColorFilter(holder.itemView.context.getColor(R.color.text))
                    } else {
                        holder.deviceDoor.setImageResource(R.drawable.ic_door_open)
                        holder.deviceDoor.setColorFilter(holder.itemView.context.getColor(R.color.red_error))
                    }

                    Log.d(
                        "DeviceAdapter",
                        "✅ IMEI:${unit.imei}, Temp:${item.temp_now}, Bat:${item.bat_status}, Door:${item.door_status}"
                    )
                } else {
                    Log.e("DeviceAdapter", "API error for IMEI:${unit.imei}, code=${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("DeviceAdapter", "Exception for IMEI:${unit.imei}, error=${e.localizedMessage}")
            }
        }

        // Navigation
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply { putString("IMEI", unit.imei) }
            it.findNavController().navigate(R.id.action_home_to_dashboard, bundle)
        }
    }

    override fun getItemCount(): Int = devices.size
}
