package com.iie.st10089153.txdevsystems_app.database.dao
import com.iie.st10089153.txdevsystems_app.database.entities.CachedConfig

import androidx.room.*

@Dao
interface ConfigDao {
    @Query("SELECT * FROM cached_config WHERE imei = :imei LIMIT 1")
    suspend fun getConfigByImei(imei: String): CachedConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: CachedConfig)

    @Query("DELETE FROM cached_config WHERE imei = :imei")
    suspend fun clearByImei(imei: String)

    @Query("DELETE FROM cached_config WHERE timestamp < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)
}