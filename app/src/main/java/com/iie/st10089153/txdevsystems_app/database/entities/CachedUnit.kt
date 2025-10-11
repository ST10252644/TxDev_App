package com.iie.st10089153.txdevsystems_app.database.entities

import androidx.room.*
import androidx.lifecycle.LiveData

@Entity(tableName = "cached_units")
data class CachedUnit(
    @PrimaryKey val imei: String,
    val name: String,
    val status: String,
    val last_seen: String,
    val timestamp: Long = System.currentTimeMillis()
)