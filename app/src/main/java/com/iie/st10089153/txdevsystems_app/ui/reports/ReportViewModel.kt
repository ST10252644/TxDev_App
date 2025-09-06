package com.iie.st10089153.txdevsystems_app.ui.reports

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ReportViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ReportsRepository(app.applicationContext)

    private val _basicCurrent = MutableLiveData<ColdRoomData?>()
    val basicCurrent: LiveData<ColdRoomData?> = _basicCurrent

    private val _extendedRange = MutableLiveData<List<ColdRoomData>>()
    val extendedRange: LiveData<List<ColdRoomData>> = _extendedRange

    private val _triggers = MutableLiveData<List<ColdRoomTriggerRow>>()
    val triggers: LiveData<List<ColdRoomTriggerRow>> = _triggers

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    // Generic error message to show in the UI
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // When true it indicates the last fetch returned no data (204 or empty list)
    private val _noData = MutableLiveData(false)
    val noData: LiveData<Boolean> = _noData

    // For specific API error codes
    private val _apiError = MutableLiveData<ApiError?>()
    val apiError: LiveData<ApiError?> = _apiError

    data class ApiError(val code: Int, val message: String?)

    fun loadBasicCurrent(imei: String) {
        _loading.value = true
        _error.value = null
        _apiError.value = null
        _noData.value = false
        viewModelScope.launch {
            try {
                _basicCurrent.value = repo.fetchCurrentBasic(imei)
            } catch (e: ReportsRepository.ApiException) {
                handleApiException(e)
                _basicCurrent.value = null
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message ?: "unknown"}"
                _basicCurrent.value = null
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadExtendedRange(imei: String, startIso: String, stopIso: String) {
        _loading.value = true
        _error.value = null
        _apiError.value = null
        _noData.value = false
        viewModelScope.launch {
            try {
                val rows = repo.fetchRangeExtended(imei, startIso, stopIso)
                if (rows.isEmpty()) {
                    // 204 or empty result
                    _extendedRange.value = emptyList()
                    _noData.value = true
                } else {
                    _extendedRange.value = rows
                    _noData.value = false
                }
            } catch (e: ReportsRepository.ApiException) {
                handleApiException(e)
                _extendedRange.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message ?: "unknown"}"
                _extendedRange.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadTriggers(imei: String, startDate: String, stopDate: String) {
        _loading.value = true
        _error.value = null
        _apiError.value = null
        _noData.value = false
        viewModelScope.launch {
            try {
                val rows = repo.fetchTriggersTable(imei, startDate, stopDate)
                if (rows.isEmpty()) {
                    _triggers.value = emptyList()
                    _noData.value = true
                } else {
                    _triggers.value = rows
                    _noData.value = false
                }
            } catch (e: ReportsRepository.ApiException) {
                handleApiException(e)
                _triggers.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message ?: "unknown"}"
                _triggers.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }

    private fun handleApiException(e: ReportsRepository.ApiException) {
        when (e.statusCode) {
            404 -> {
                _apiError.value = ApiError(404, "Unit not found or not authorized")
            }
            422 -> {
                _apiError.value = ApiError(422, e.message ?: "Validation error occurred")
            }
            else -> {
                _error.value = e.message ?: "API error occurred"
            }
        }
    }
}