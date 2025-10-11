package com.iie.st10089153.txdevsystems_app.database.entities

import androidx.room.*
import androidx.lifecycle.LiveData

@Entity(tableName = "cached_notifications")
data class CachedNotification(
    @PrimaryKey val id: String,
    val imei: String,
    val device_name: String,
    val title: String,
    val message: String,
    val type: String,
    val notification_timestamp: String,
    val is_read: Boolean = false,
    val cached_at: Long = System.currentTimeMillis()
)