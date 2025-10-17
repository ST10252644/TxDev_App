package com.iie.st10089153.txdevsystems_app.repository

import android.content.Context
import android.util.Log
import com.iie.st10089153.txdevsystems_app.database.AppDatabase
import com.iie.st10089153.txdevsystems_app.network.Api.CurrentRequest  // ✅ Specific import
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem  // ✅ Use UI version
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for dashboard/current data with aggressive caching
 */
class DashboardRepository(private val context: Context) {

    private val api = RetrofitClient.getDashboardApi(context)
    private val db = AppDatabase.getDatabase(context)
    private val dashboardDao = db.dashboardDao()

    companion object {
        private const val TAG = "DashboardRepository"
        private const val CACHE_VALIDITY_MS = 1 * 60 * 1000L // 1 minute
    }

    suspend fun getCurrentData(imei: String, forceRefresh: Boolean = false): DashboardItem? =
        withContext(Dispatchers.IO) {
            try {
                // Check cache
                val cached = dashboardDao.getDashboardByImei(imei)
                val isCacheValid = cached != null &&
                        (System.currentTimeMillis() - cached.timestamp) < CACHE_VALIDITY_MS

                if (isCacheValid && !forceRefresh) {
                    Log.d(TAG, "Returning dashboard from cache for $imei")
                    return@withContext cached!!.toDashboardItem()
                }

                // Fetch from API
                Log.d(TAG, "Fetching dashboard from API for $imei")
                val response = api.getCurrent(CurrentRequest(imei))

                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    // Cache it
                    dashboardDao.insertDashboard(data.toCachedDashboard())

                    return@withContext data
                } else {
                    // Return stale cache if available
                    return@withContext cached?.toDashboardItem()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching dashboard for $imei", e)
                // Return stale cache on error
                return@withContext dashboardDao.getDashboardByImei(imei)?.toDashboardItem()
            }
        }

    suspend fun invalidateCache(imei: String) = withContext(Dispatchers.IO) {
        dashboardDao.clearByImei(imei)
    }
}