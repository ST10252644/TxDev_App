package com.iie.st10089153.txdevsystems_app.ui.notifications

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