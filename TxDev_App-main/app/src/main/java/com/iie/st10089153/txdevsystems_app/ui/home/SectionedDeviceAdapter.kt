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

class SectionedDeviceAdapter(private val items: List<SectionItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    sealed class SectionItem {
        data class Header(val title: String, val count: Int) : SectionItem()
        data class Device(val unit: AvailableUnit) : SectionItem()
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val headerTitle: TextView = view.findViewById(R.id.headerTitle)
    }

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val deviceTemp: TextView = view.findViewById(R.id.deviceTemp)
        val deviceStatus: TextView = view.findViewById(R.id.deviceStatus)
        val deviceLastSeen: TextView = view.findViewById(R.id.deviceLastSeen)
        val deviceBattery: ImageView = view.findViewById(R.id.deviceBattery)
        val deviceDoor: ImageView = view.findViewById(R.id.deviceDoor)
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is SectionItem.Header -> TYPE_HEADER
            is SectionItem.Device -> TYPE_DEVICE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.section_header, parent, false)
                HeaderViewHolder(view)
            }
            TYPE_DEVICE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.device_card, parent, false)
                DeviceViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is SectionItem.Header -> {
                val headerHolder = holder as HeaderViewHolder
                headerHolder.headerTitle.text = "${item.title} (${item.count})"
            }
            is SectionItem.Device -> {
                val deviceHolder = holder as DeviceViewHolder
                bindDevice(deviceHolder, item.unit)
            }
        }
    }

    private fun bindDevice(holder: DeviceViewHolder, unit: AvailableUnit) {
        // Bind static fields
        holder.deviceName.text = unit.name
        holder.deviceStatus.text =
            if (unit.status.equals("Active", ignoreCase = true)) "● Online" else "● Offline"
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
                        "SectionedDeviceAdapter",
                        "API error for IMEI:${unit.imei}, code=${response.code()}"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "SectionedDeviceAdapter",
                    "Exception for IMEI:${unit.imei}, error=${e.localizedMessage}"
                )
            }
        }

        // Navigate to Dashboard with the selected IMEI
        holder.itemView.setOnClickListener { v ->
            v.findNavController().navigate(
                R.id.action_home_to_dashboard,
                bundleOf("IMEI" to unit.imei, "name" to unit.name)
            )
        }
    }

    override fun getItemCount(): Int = items.size

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DEVICE = 1

        fun createSectionedList(devices: List<AvailableUnit>): List<SectionItem> {
            val items = mutableListOf<SectionItem>()

            // Separate online and offline devices
            val onlineDevices = devices.filter { it.status.equals("Active", ignoreCase = true) }
            val offlineDevices = devices.filter { !it.status.equals("Active", ignoreCase = true) }

            // Add online section
            if (onlineDevices.isNotEmpty()) {
                items.add(SectionItem.Header("Online Devices", onlineDevices.size))
                onlineDevices.forEach { items.add(SectionItem.Device(it)) }
            }

            // Add offline section
            if (offlineDevices.isNotEmpty()) {
                items.add(SectionItem.Header("Offline Devices", offlineDevices.size))
                offlineDevices.forEach { items.add(SectionItem.Device(it)) }
            }

            return items
        }
    }
}