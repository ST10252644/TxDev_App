package com.iie.st10089153.txdevsystems_app.ui.dashboard.models

data class TempGauge(
    val name: String = "Temperature",
    val iconRes: Int,
    val statusText: String?,   // current temperature as string
    val measurement: String?,  // e.g., "°C"
    val minValue: Float?,      // minimum temperature
    val maxValue: Float?,      // maximum temperature
    val safeMin: Float?,       // safe zone start
    val safeMax: Float?        // safe zone end
)
