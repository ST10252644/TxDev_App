package com.iie.st10089153.txdevsystems_app.repository

import android.content.Context
import android.util.Log
import com.iie.st10089153.txdevsystems_app.database.AppDatabase
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsRequest  // ✅ Use the one from network.Api
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for managing device/unit data with caching
 * Uses cache-first strategy with automatic refresh
 */
class UnitsRepository(private val context: Context) {

    private val api = RetrofitClient.getAvailableUnitsApi(context)
    private val db = AppDatabase.getDatabase(context)
    private val unitDao = db.unitDao()

    companion object {
        private const val TAG = "UnitsRepository"
        private const val CACHE_VALIDITY_MS = 2 * 60 * 1000L // 2 minutes for home screen
    }

    /**
     * Get units with cache-first strategy
     * Returns cached data immediately if valid, then fetches fresh data in background
     */
    suspend fun getUnits(forceRefresh: Boolean = false): List<AvailableUnit> = withContext(Dispatchers.IO) {
        try {
            // Check cache validity
            val cacheValidTime = System.currentTimeMillis() - CACHE_VALIDITY_MS
            val cachedUnits = if (!forceRefresh) {
                unitDao.getValidUnits(cacheValidTime)
            } else {
                emptyList()
            }

            // Return cache if valid and not force refresh
            if (cachedUnits.isNotEmpty() && !forceRefresh) {
                Log.d(TAG, "Returning ${cachedUnits.size} units from cache")
                return@withContext cachedUnits.map { it.toAvailableUnit() }
            }

            // Fetch from API
            Log.d(TAG, "Fetching units from API")
            val response = api.getAvailableUnits(AvailableUnitsRequest(status = "All"))

            if (response.isSuccessful && response.body() != null) {
                val units = response.body()!!

                // Cache the results
                val cachedEntities = units.map { it.toCachedUnit() }
                unitDao.insertUnits(cachedEntities)

                // Clean old cache
                unitDao.deleteExpiredUnits(System.currentTimeMillis() - (24 * 60 * 60 * 1000L))

                Log.d(TAG, "Cached ${units.size} units")
                return@withContext units
            } else {
                Log.e(TAG, "API error: ${response.code()}")
                // Return stale cache if API fails
                return@withContext cachedUnits.map { it.toAvailableUnit() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching units", e)
            // Return any available cache on error
            val staleCache = unitDao.getValidUnits(0) // Get all cache
            return@withContext staleCache.map { it.toAvailableUnit() }
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.IO) {
        unitDao.clearAll()
    }
}