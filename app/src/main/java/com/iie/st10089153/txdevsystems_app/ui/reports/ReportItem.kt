package com.iie.st10089153.txdevsystems_app.ui.reports

data class ReportItem(

    val tempNow: String,
    val doorStatus: DoorStatus,
    val powerStatus: PowerStatus,
    val batteryStatus: BatteryStatus,
    val timestamp: String
)

enum class DoorStatus {
    OPEN, CLOSED
}

enum class PowerStatus {
    OK, ERROR
}

enum class BatteryStatus {
    OK, LOW, ERROR
}