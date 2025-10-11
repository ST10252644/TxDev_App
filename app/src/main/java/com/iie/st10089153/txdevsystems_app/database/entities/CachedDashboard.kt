package com.iie.st10089153.txdevsystems_app.database.entities

import androidx.room.*
import androidx.lifecycle.LiveData

@Entity(tableName = "cached_dashboard")
data class CachedDashboard(
    @PrimaryKey val imei: String,
    val temp_now: String,
    val temp_min: String,
    val temp_max: String,
    val door_status: String,
    val door_status_bool: Int,
    val supply_status: String,
    val bat_volt: String,
    val bat_status: String,
    val timestamp: Long = System.currentTimeMillis()
)