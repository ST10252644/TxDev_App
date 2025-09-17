package com.iie.st10089153.txdevsystems_app.ui.dashboard.models

data class DashboardResponse(
    val supply_status: String,
    val bat_volt: Double,
    val temp_now: Double,
    val door_status: String
)
