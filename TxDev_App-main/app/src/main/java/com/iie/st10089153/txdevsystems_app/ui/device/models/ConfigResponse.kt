package com.iie.st10089153.txdevsystems_app.ui.device.models

data class ConfigResponse(
    val imei: String,
    val unit_id: String,
    val temp_max: String,
    val temp_min: String,
    val door_alarm_hour: String,
    val door_alarm_min: String,
    val switch_polarity: String
)
