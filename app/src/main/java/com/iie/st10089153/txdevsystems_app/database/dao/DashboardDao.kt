package com.iie.st10089153.txdevsystems_app.database.dao
import com.iie.st10089153.txdevsystems_app.database.entities.CachedDashboard

import androidx.room.*

@Dao
interface DashboardDao {
    @Query("SELECT * FROM cached_dashboard WHERE imei = :imei LIMIT 1")
    suspend fun getDashboardByImei(imei: String): CachedDashboard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashboard(dashboard: CachedDashboard)

    @Query("DELETE FROM cached_dashboard WHERE timestamp < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)

    @Query("DELETE FROM cached_dashboard WHERE imei = :imei")
    suspend fun clearByImei(imei: String)
}