package com.iie.st10089153.txdevsystems_app.database.entities

import androidx.room.*
import androidx.lifecycle.LiveData

@Entity(tableName = "cached_config")
data class CachedConfig(
    @PrimaryKey val imei: String,
    val unit_id: String?,
    val temp_max: String?,
    val temp_min: String?,
    val door_alarm_hour: String?,
    val door_alarm_min: String?,
    val switch_polarity: String?,
    val config_type: String?,
    val data_resend_min: String?,
    val network: String?,
    val remaining_data: String?,
    val timestamp: Long = System.currentTimeMillis()
)