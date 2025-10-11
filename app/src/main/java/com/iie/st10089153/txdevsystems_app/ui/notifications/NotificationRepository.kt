package com.iie.st10089153.txdevsystems_app.ui.notifications

import android.content.Context
import android.util.Log
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit  // ✅ From network.Api
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsRequest  // ✅ From network.Api
import com.iie.st10089153.txdevsystems_app.network.Api.Trigger  // ✅ From network.Api
import com.iie.st10089153.txdevsystems_app.network.Api.TriggersRequest  // ✅ From network.Api
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*

class NotificationsRepository(private val context: Context) {

    private val api = RetrofitClient.getNotificationsApi(context)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    suspend fun fetchAllNotifications(): List<NotificationItem> {
        val notifications = mutableListOf<NotificationItem>()

        try {
            // 1. Fetch all units
            val unitsResponse = api.getAvailableUnits(AvailableUnitsRequest(status = "All"))
            if (!unitsResponse.isSuccessful) {
                Log.e("NotificationsRepo", "Failed to fetch units: ${unitsResponse.message()}")
                return emptyList()
            }

            val units = unitsResponse.body() ?: return emptyList()
            Log.d("NotificationsRepo", "Fetched ${units.size} units")

            // 2. Get last 7 days date range
            val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val endDate = dateTimeFormat.format(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val startDate = dateTimeFormat.format(calendar.time)

            // 3. Loop through units and check triggers
            for (unit in units) {
                try {
                    val request = TriggersRequest(
                        imei = unit.imei,
                        start = startDate,
                        stop = endDate
                    )

                    val response = api.getTriggers(request)
                    if (response.isSuccessful) {
                        response.body()?.forEach { trigger ->
                            addAlertsFromTrigger(unit, trigger, notifications)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("NotificationsRepo", "Error fetching triggers for ${unit.name}", e)
                }
            }

        } catch (e: Exception) {
            Log.e("NotificationsRepo", "Error fetching notifications", e)
        }

        return notifications.sortedByDescending { it.timestamp }
    }

    private fun addAlertsFromTrigger(
        unit: AvailableUnit,
        trigger: Trigger,
        notifications: MutableList<NotificationItem>
    ) {
        // Your existing implementation stays the same
        if (trigger.door_trigger != "Door Okay") {
            notifications.add(
                NotificationItem(
                    id = "${trigger.id}_door",
                    title = "Door Alert",
                    message = "${unit.name}: ${trigger.door_trigger}",
                    timestamp = trigger.timestamp,
                    deviceName = unit.name,
                    imei = unit.imei,
                    type = NotificationType.DOOR_OPEN
                )
            )
        }

        if (trigger.temp_trigger != "Temp Okay") {
            val temp = trigger.temp?.trim() ?: "Unknown"
            val alertType = when (trigger.temp_trigger) {
                "High Temp Warning" -> NotificationType.TEMPERATURE_HIGH
                "Low Temp Warning" -> NotificationType.TEMPERATURE_LOW
                else -> NotificationType.TEMPERATURE_LOW
            }

            notifications.add(
                NotificationItem(
                    id = "${trigger.id}_temp",
                    title = if (alertType == NotificationType.TEMPERATURE_HIGH) "High Temperature Alert" else "Low Temperature Alert",
                    message = "${unit.name}: $temp°C (${trigger.temp_trigger})",
                    timestamp = trigger.timestamp,
                    deviceName = unit.name,
                    imei = unit.imei,
                    type = alertType
                )
            )
        }

        if (trigger.supply_trigger != "Mains Okay") {
            notifications.add(
                NotificationItem(
                    id = "${trigger.id}_power",
                    title = "Power Alert",
                    message = "${unit.name}: ${trigger.supply_trigger}",
                    timestamp = trigger.timestamp,
                    deviceName = unit.name,
                    imei = unit.imei,
                    type = NotificationType.POWER_FAILURE
                )
            )
        }

        if (trigger.bat_low != "Bat Okay") {
            val voltage = trigger.supply_volt ?: "Unknown"
            notifications.add(
                NotificationItem(
                    id = "${trigger.id}_battery",
                    title = "Battery Alert",
                    message = "${unit.name}: $voltage V (${trigger.bat_low})",
                    timestamp = trigger.timestamp,
                    deviceName = unit.name,
                    imei = unit.imei,
                    type = NotificationType.BATTERY_LOW
                )
            )
        }
    }
}