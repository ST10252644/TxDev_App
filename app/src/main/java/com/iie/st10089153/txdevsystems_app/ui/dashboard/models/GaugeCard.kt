package com.iie.st10089153.txdevsystems_app.ui.dashboard.models

data class GaugeCard(
    val iconRes: Int,
    val statusText: String,
    val name: String,
    val measurement: String? = null,
    val gaugeImageRes: Int,
    val minValue: Float? = null,
    val maxValue: Float? = null,
    val type: String = "" // Added type: "temperature", "battery", or "" for normal
)
