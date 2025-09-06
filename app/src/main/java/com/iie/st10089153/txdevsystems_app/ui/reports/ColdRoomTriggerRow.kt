package com.iie.st10089153.txdevsystems_app.ui.reports

data class ColdRoomTriggerRow(
    val deviceid: String,
    val temp_now: Double?, // from Trigger.temp (nullable)
    val min_temp: Double?, // from Trigger.temp_set_min
    val max_temp: Double?, // from Trigger.temp_set_max
    val timestamp: String
)