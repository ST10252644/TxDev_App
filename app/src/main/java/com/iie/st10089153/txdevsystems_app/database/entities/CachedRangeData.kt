package com.iie.st10089153.txdevsystems_app.database.entities

import androidx.room.*
import androidx.lifecycle.LiveData

@Entity(
    tableName = "cached_range_data",
    indices = [Index(value = ["imei", "data_timestamp"])]
)
data class CachedRangeData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imei: String,
    val temp_now: String,
    val temp_min: String,
    val temp_max: String,
    val door_status: String?,
    val door_status_bool: String?,
    val supply_status: String?,
    val bat_status: String?,
    val data_timestamp: String, // The actual data timestamp from API
    val cached_at: Long = System.currentTimeMillis()
)