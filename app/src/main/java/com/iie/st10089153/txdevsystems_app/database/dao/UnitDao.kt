package com.iie.st10089153.txdevsystems_app.database.dao
import com.iie.st10089153.txdevsystems_app.database.entities.CachedUnit

import androidx.room.*
import androidx.lifecycle.LiveData

@Dao
interface UnitDao {
    @Query("SELECT * FROM cached_units ORDER BY status DESC, name ASC")
    fun getAllUnits(): LiveData<List<CachedUnit>>

    @Query("SELECT * FROM cached_units WHERE timestamp > :validAfter")
    suspend fun getValidUnits(validAfter: Long): List<CachedUnit>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUnits(units: List<CachedUnit>)

    @Query("DELETE FROM cached_units WHERE timestamp < :expiredBefore")
    suspend fun deleteExpiredUnits(expiredBefore: Long)

    @Query("DELETE FROM cached_units")
    suspend fun clearAll()
}