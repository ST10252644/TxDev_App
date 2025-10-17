package com.iie.st10089153.txdevsystems_app.network.Api

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