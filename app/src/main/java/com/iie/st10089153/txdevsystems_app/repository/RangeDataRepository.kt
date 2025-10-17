package com.iie.st10089153.txdevsystems_app.repository

import android.content.Context
import android.util.Log
import com.iie.st10089153.txdevsystems_app.database.AppDatabase
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint  // ✅ Your actual class
import com.iie.st10089153.txdevsystems_app.network.Api.RangeRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RangeDataRepository(private val context: Context) {

    private val api = RetrofitClient.getRangeApi(context)
    private val db = AppDatabase.getDatabase(context)
    private val rangeDao = db.rangeDataDao()

    companion object {
        private const val TAG = "RangeDataRepository"
        private const val CACHE_VALIDITY_MS = 10 * 60 * 1000L // 10 minutes
    }

    suspend fun getRangeData(
        imei: String,
        startTime: String,
        endTime: String,
        forceRefresh: Boolean = false
    ): List<RangePoint> = withContext(Dispatchers.IO) {
        try {
            // Check cache for this time range
            val cachedData = rangeDao.getRangeData(imei, startTime, endTime)

            // Check if cache is recent enough
            val isCacheValid = cachedData.isNotEmpty() &&
                    cachedData.all { (System.currentTimeMillis() - it.cached_at) < CACHE_VALIDITY_MS }

            if (isCacheValid && !forceRefresh) {
                Log.d(TAG, "Returning ${cachedData.size} range data points from cache")
                return@withContext cachedData.map { it.toRangePoint() }
            }

            // Fetch from API
            Log.d(TAG, "Fetching range data from API")
            val response = api.fetchRange(RangeRequest(imei, startTime, endTime))

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!

                // Cache the data
                val cachedEntities = data.map { it.toCachedRangeData(imei) }
                rangeDao.insertRangeData(cachedEntities)

                // Clean old cache (keep last 7 days)
                rangeDao.deleteExpired(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L))

                Log.d(TAG, "Cached ${data.size} range data points")
                return@withContext data
            } else {
                // Return stale cache if available
                return@withContext cachedData.map { it.toRangePoint() }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching range data", e)
            // Return any cached data on error
            return@withContext rangeDao.getRangeData(imei, startTime, endTime)
                .map { it.toRangePoint() }
        }
    }
}