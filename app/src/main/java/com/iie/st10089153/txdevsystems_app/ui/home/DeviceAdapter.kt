package com.iie.st10089153.txdevsystems_app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
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

        // Bind static fields
        holder.deviceName.text = unit.name


        if (unit.status.equals("Active", ignoreCase = true)) {
            holder.deviceStatus.text = "● Online"
            holder.deviceStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary)
            )
        } else {
            holder.deviceStatus.text = "● Offline"
            holder.deviceStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.orange_offline)
            )
        }


        holder.deviceLastSeen.text = "Last refreshed: ${unit.last_seen}"

        // Keep track of which IMEI this view is currently showing
        holder.itemView.tag = unit.imei

        // Fetch live dashboard snapshot for this card
        val ctx = holder.itemView.context
        val activity = (ctx as? FragmentActivity) ?: return
        activity.lifecycleScope.launch {
            try {
                val api = RetrofitClient.getDashboardApi(ctx)
                val response = withContext(Dispatchers.IO) {
                    api.getCurrent(CurrentRequest(unit.imei))
                }

                if (response.isSuccessful && response.body() != null) {
                    // If the view was recycled to another item, ignore this result
                    if (holder.itemView.tag != unit.imei) return@launch

                    val item = response.body()!!

                    // Temperature
                    holder.deviceTemp.text = "${item.temp_now}"
                    holder.deviceTemp.setTextColor(
                        ContextCompat.getColor(ctx, R.color.primary)
                    )

                    // Battery
                    if (item.bat_status.equals("Okay", ignoreCase = true)) {
                        holder.deviceBattery.setImageResource(R.drawable.ic_battery_full)
                        holder.deviceBattery.setColorFilter(
                            ContextCompat.getColor(ctx, R.color.primary)
                        )
                    } else {
                        holder.deviceBattery.setImageResource(R.drawable.ic_battery_empty)
                        holder.deviceBattery.setColorFilter(
                            ContextCompat.getColor(ctx, R.color.red_error)
                        )
                    }

                    // Door
                    if (item.door_status_bool == 0) {
                        holder.deviceDoor.setImageResource(R.drawable.ic_door_close)
                        holder.deviceDoor.setColorFilter(
                            ContextCompat.getColor(ctx, R.color.text)
                        )
                    } else {
                        holder.deviceDoor.setImageResource(R.drawable.ic_door_open)
                        holder.deviceDoor.setColorFilter(
                            ContextCompat.getColor(ctx, R.color.red_error)
                        )
                    }

                } else {
                    android.util.Log.e(
                        "DeviceAdapter",
                        "API error for IMEI:${unit.imei}, code=${response.code()}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "DeviceAdapter",
                    "Exception for IMEI:${unit.imei}, error=${e.localizedMessage}"
                )
            }
        }

        // Navigate to Dashboard with the selected IMEI (this is what your charts pick up later)
        holder.itemView.setOnClickListener { v ->
            v.findNavController().navigate(
                R.id.action_home_to_dashboard,
                bundleOf("IMEI" to unit.imei, "name" to unit.name)
            )
        }
    }

    override fun getItemCount(): Int = devices.size
}
