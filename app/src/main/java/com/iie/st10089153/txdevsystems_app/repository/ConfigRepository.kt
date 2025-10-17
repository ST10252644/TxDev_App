package com.iie.st10089153.txdevsystems_app.repository

import android.content.Context
import android.util.Log
import com.iie.st10089153.txdevsystems_app.database.*
import com.iie.st10089153.txdevsystems_app.network.Api.*
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.device.models.ConfigResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository for device configuration with long cache duration
 */
class ConfigRepository(private val context: Context) {

    private val api = RetrofitClient.getDeviceApi(context)
    private val db = AppDatabase.getDatabase(context)
    private val configDao = db.configDao()

    companion object {
        private const val TAG = "ConfigRepository"
        private const val CACHE_VALIDITY_MS = 5 * 60 * 1000L // 5 minutes (config changes rarely)
    }

    suspend fun getConfig(imei: String, forceRefresh: Boolean = false): ConfigResponse? =
        withContext(Dispatchers.IO) {
            try {
                val cached = configDao.getConfigByImei(imei)
                val isCacheValid = cached != null &&
                        (System.currentTimeMillis() - cached.timestamp) < CACHE_VALIDITY_MS

                if (isCacheValid && !forceRefresh) {
                    Log.d(TAG, "Returning config from cache for $imei")
                    return@withContext cached!!.toConfigResponse()
                }

                Log.d(TAG, "Fetching config from API for $imei")
                val response = api.getConfigByImei(ConfigByImeiRequest(imei))

                if (response.isSuccessful && response.body() != null) {
                    val config = response.body()!!

                    // Cache it
                    configDao.insertConfig(config.toCachedConfig())

                    return@withContext config
                } else {
                    return@withContext cached?.toConfigResponse()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching config for $imei", e)
                return@withContext configDao.getConfigByImei(imei)?.toConfigResponse()
            }
        }

    suspend fun invalidateCache(imei: String) = withContext(Dispatchers.IO) {
        configDao.clearByImei(imei)
    }
}