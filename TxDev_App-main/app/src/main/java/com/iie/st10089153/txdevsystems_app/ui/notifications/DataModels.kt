package com.iie.st10089153.txdevsystems_app.ui.notifications

// Available Units Response
data class AvailableUnit(
    val imei: String,
    val status: String,
    val name: String,
    val last_seen: String
)

data class AvailableUnitsRequest(
    val status: String = "Active"
)

// Triggers Response
data class Trigger(
    val id: Int,
    val timestamp: String,
    val imei: String,
    val temp: String?,
    val unit_id: String?,
    val telegram_no: String?,
    val supply_volt: String?,
    val temp_set_max: String?,
    val temp_set_min: String?,
    val supply_trigger: String?,
    val door_trigger: String?,
    val bat_low: String?,
    val temp_trigger: String?
)

data class TriggersRequest(
    val door_trigger: String? = null,
    val temp_trigger: String? = null,
    val supply_trigger: String? = null,
    val bat_low: String? = null,
    val imei: String,
    val limit: Int = 50,
    val skip: Int = 0,
    val start: String,
    val stop: String
)

// Notification Data Class
data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val deviceName: String,
    val imei: String,
    val type: NotificationType,
    val isRead: Boolean = false
)
enum class NotificationType {
    DOOR_OPEN,
    TEMPERATURE_HIGH,
    TEMPERATURE_LOW,
    POWER_FAILURE,
    BATTERY_LOW
}
