package com.iie.st10089153.txdevsystems_app.ui.reports

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.launch

class ReportViewModel(app: Application) : AndroidViewModel(app) {


    private val repo = ReportsRepository(app.applicationContext)

    private val _reportData = MutableLiveData<List<ReportItem>>()
    val reportData: LiveData<List<ReportItem>> = _reportData

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRangeData(imei: String, startIso: String, stopIso: String) {
        _loading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val data = repo.fetchRangeData(imei, startIso, stopIso)
                _reportData.value = data

                if (data.isEmpty()) {
                    _error.value = "No data available for selected time range"
                }
            } catch (e: ReportsRepository.ApiException) {
                when (e.statusCode) {
                    404 -> _error.value = "Unit not found or not authorized"
                    422 -> _error.value = "Validation error: ${e.message}"
                    else -> _error.value = "API error: ${e.message}"
                }
                _reportData.value = emptyList()
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
                _reportData.value = emptyList()
            } finally {
                _loading.value = false
            }
        }
    }
}