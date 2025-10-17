package com.iie.st10089153.txdevsystems_app.database.dao
import com.iie.st10089153.txdevsystems_app.database.entities.CachedRangeData

import androidx.room.*

@Dao
interface RangeDataDao {
    @Query("""
        SELECT * FROM cached_range_data 
        WHERE imei = :imei 
        AND data_timestamp BETWEEN :startTime AND :endTime
        ORDER BY data_timestamp DESC
    """)
    suspend fun getRangeData(imei: String, startTime: String, endTime: String): List<CachedRangeData>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRangeData(data: List<CachedRangeData>)

    @Query("DELETE FROM cached_range_data WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)

    @Query("DELETE FROM cached_range_data WHERE imei = :imei")
    suspend fun clearByImei(imei: String)

    @Query("SELECT COUNT(*) FROM cached_range_data WHERE imei = :imei")
    suspend fun getDataCount(imei: String): Int
}