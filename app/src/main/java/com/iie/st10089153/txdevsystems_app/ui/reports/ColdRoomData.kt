package com.iie.st10089153.txdevsystems_app.ui.reports

data class ColdRoomData(
    val deviceid: String,
    val temp_now: Double,
    val min_temp: Double?,
    val max_temp: Double?,
    val supply_status: String?,
    val batt_status: String?,
    val door_status: String?,
    val timestamp: String
)