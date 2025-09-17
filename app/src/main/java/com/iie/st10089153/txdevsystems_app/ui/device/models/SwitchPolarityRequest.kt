package com.iie.st10089153.txdevsystems_app.ui.device.models

data class SwitchPolarityRequest(
    val imei: String,
    val switch_polarity: String // "0" = NC, "1" = NO
)