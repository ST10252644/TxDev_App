package com.iie.st10089153.txdevsystems_app.ui.device.models

data class TempThresholdRequest(
    val imei: String,
    val max: String,
    val min: String
)