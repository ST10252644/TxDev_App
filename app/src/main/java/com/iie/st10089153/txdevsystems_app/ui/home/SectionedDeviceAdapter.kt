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
import com.iie.st10089153.txdevsystems_app.network.Api.RangeRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

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

    private fun checkTemperatureRange(
        holder: DeviceViewHolder,
        imei: String,
        currentTemp: String,
        isOnline: Boolean,
        context: android.content.Context
    ) {
        val activity = (context as? FragmentActivity) ?: return

        activity.lifecycleScope.launch {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                val currentTime = Date()
                val oneDayAgo = Date(currentTime.time - 24 * 60 * 60 * 1000)

                val rangeApi = RetrofitClient.getRangeApi(context)
                val rangeResponse = withContext(Dispatchers.IO) {
                    rangeApi.fetchRange(
                        RangeRequest(
                            imei = imei,
                            start = dateFormat.format(oneDayAgo),
                            stop = dateFormat.format(currentTime)
                        )
                    )
                }

                if (rangeResponse.isSuccessful && !rangeResponse.body().isNullOrEmpty()) {
                    if (holder.itemView.tag != imei) return@launch

                    val rangeData = rangeResponse.body()!!
                    val latestRangePoint = rangeData.maxByOrNull { it.timestamp }

                    if (latestRangePoint != null) {
                        val tempMin = parseTemperature(latestRangePoint.temp_min)
                        val tempMax = parseTemperature(latestRangePoint.temp_max)
                        val tempNow = parseTemperature(currentTemp)

                        if (tempMin != null && tempMax != null && tempNow != null) {
                            // Decide color based on range
                            val colorRes = when {
                                !isOnline -> R.color.orange_offline //
                                tempNow < tempMin -> R.color.blue_cool //
                                tempNow > tempMax -> R.color.red_error //
                                else -> R.color.primary              //
                            }

                            holder.deviceTemp.setTextColor(ContextCompat.getColor(context, colorRes))

                            android.util.Log.d(
                                "TemperatureRange",
                                "IMEI: $imei, Current: $tempNow, Range: $tempMin-$tempMax, Color: $colorRes"
                            )
                        } else {
                            fallbackTemperatureColor(holder, isOnline, context)
                        }
                    } else {
                        fallbackTemperatureColor(holder, isOnline, context)
                    }
                } else {
                    android.util.Log.w(
                        "TemperatureRange",
                        "Range API failed for IMEI: $imei, code: ${rangeResponse.code()}"
                    )
                    fallbackTemperatureColor(holder, isOnline, context)
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "TemperatureRange",
                    "Exception checking temperature range for IMEI: $imei, error: ${e.localizedMessage}"
                )
                fallbackTemperatureColor(holder, isOnline, context)
            }
        }
    }


    private fun parseTemperature(temp: Any?): Double? {
        return when (temp) {
            is Number -> temp.toDouble()
            is String -> temp.toDoubleOrNull()
            else -> null
        }
    }

    private fun fallbackTemperatureColor(
        holder: DeviceViewHolder,
        isOnline: Boolean,
        context: android.content.Context
    ) {
        // Original logic: green for online, orange for offline
        if (isOnline) {
            holder.deviceTemp.setTextColor(
                ContextCompat.getColor(context, R.color.primary)
            )
        } else {
            holder.deviceTemp.setTextColor(
                ContextCompat.getColor(context, R.color.orange_offline)
            )
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

                // Set header color based on section type
                if (item.title.contains("Offline", ignoreCase = true)) {
                    headerHolder.headerTitle.setTextColor(
                        ContextCompat.getColor(headerHolder.itemView.context, R.color.orange_offline)
                    )
                } else {
                    // Online devices header - keep default color or use primary
                    headerHolder.headerTitle.setTextColor(
                        ContextCompat.getColor(headerHolder.itemView.context, R.color.primary)
                    )
                }
            }
            is SectionItem.Device -> {
                val deviceHolder = holder as DeviceViewHolder
                bindDevice(deviceHolder, item.unit)
            }
        }
    }

    private fun bindDevice(holder: DeviceViewHolder, unit: AvailableUnit) {
        holder.deviceName.text = unit.name

        // Determine if device is online or offline
        val isOnline = unit.status.equals("Active", ignoreCase = true)

        if (isOnline) {
            holder.deviceStatus.text = "● Online"
            holder.deviceStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primary)
            )
            holder.deviceStatus.setShadowLayer(
                6f,  // blur radius (similar to your Figma "blur: 4")
                0f,  // x-offset (no horizontal shift)
                0f,  // y-offset (no vertical shift)
                ContextCompat.getColor(holder.itemView.context, R.color.primary) // glow color
            )
        } else {
            holder.deviceStatus.text = "● Offline"
            holder.deviceStatus.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.orange_offline)
            )
            holder.deviceStatus.setShadowLayer(
                6f, 0f, 0f,
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

                    // Temperature with range checking
                    holder.deviceTemp.text = "${item.temp_now}"

                    // Check temperature range and set appropriate color
                    checkTemperatureRange(holder, unit.imei, item.temp_now.toString(), isOnline, ctx)

                    // Battery - Use different colors based on online/offline status
                    if (isOnline) {
                        // Online device: use normal green/red logic
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
                    } else {
                        // Offline device: use orange/white colors instead of green/red
                        if (item.bat_status.equals("Okay", ignoreCase = true)) {
                            holder.deviceBattery.setImageResource(R.drawable.ic_battery_full)
                            holder.deviceBattery.setColorFilter(
                                ContextCompat.getColor(ctx, R.color.orange_offline)
                            )
                        } else {
                            holder.deviceBattery.setImageResource(R.drawable.ic_battery_empty)
                            holder.deviceBattery.setColorFilter(
                                ContextCompat.getColor(ctx, R.color.text) // Use white/gray instead of red
                            )
                        }
                    }

                    // Door - Keep similar logic but could also be adjusted for offline
                    if (item.door_status_bool == 0) {
                        holder.deviceDoor.setImageResource(R.drawable.ic_door_close)
                        holder.deviceDoor.setColorFilter(
                            ContextCompat.getColor(ctx, R.color.text)
                        )
                    } else {
                        holder.deviceDoor.setImageResource(R.drawable.ic_door_open)
                        if (isOnline) {
                            holder.deviceDoor.setColorFilter(
                                ContextCompat.getColor(ctx, R.color.red_error)
                            )
                        } else {
                            // For offline devices, use orange for open door instead of red
                            holder.deviceDoor.setColorFilter(
                                ContextCompat.getColor(ctx, R.color.orange_offline)
                            )
                        }
                    }

                } else {
                    android.util.Log.e(
                        "SectionedDeviceAdapter",
                        "API error for IMEI:${unit.imei}, code=${response.code()}"
                    )

                    // Set default colors for offline devices when API fails
                    if (!isOnline) {
                        holder.deviceTemp.text = "--"
                        holder.deviceTemp.setTextColor(
                            ContextCompat.getColor(ctx, R.color.text)
                        )
                        holder.deviceBattery.setColorFilter(
                            ContextCompat.getColor(ctx, R.color.text)
                        )
                        holder.deviceDoor.setColorFilter(
                            ContextCompat.getColor(ctx, R.color.text)
                        )
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e(
                    "SectionedDeviceAdapter",
                    "Exception for IMEI:${unit.imei}, error=${e.localizedMessage}"
                )

                // Set default colors for offline devices when exception occurs
                if (!isOnline) {
                    holder.deviceTemp.text = "--"
                    holder.deviceTemp.setTextColor(
                        ContextCompat.getColor(ctx, R.color.text)
                    )
                    holder.deviceBattery.setColorFilter(
                        ContextCompat.getColor(ctx, R.color.text)
                    )
                    holder.deviceDoor.setColorFilter(
                        ContextCompat.getColor(ctx, R.color.text)
                    )
                }
            }
        }

        // Navigate to Dashboard with the selected IMEI
        holder.itemView.setOnClickListener { v ->
            v.findNavController().navigate(
                R.id.action_home_to_dashboard,
                bundleOf("IMEI" to unit.imei, "name" to unit.name, "last_seen" to unit.last_seen)
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