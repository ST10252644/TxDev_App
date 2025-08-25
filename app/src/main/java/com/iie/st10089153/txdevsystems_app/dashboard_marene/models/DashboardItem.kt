package dashboard_marene.models

data class DashboardItem(
    val id: Int,
    val imei: String,
    val temp_max: Float,
    val temp_now: Float,
    val temp_min: Float,
    val supply_volt: Float,
    val bat_volt: Float,
    val supply_status: String,
    val bat_status: String,
    val door_status: String,
    val door_status_bool: Int,
    val signal_strength: String,
    val timestamp: String,
    val active: Int
)
