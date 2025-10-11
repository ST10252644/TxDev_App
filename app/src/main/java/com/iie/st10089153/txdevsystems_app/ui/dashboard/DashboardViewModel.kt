package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iie.st10089153.txdevsystems_app.network.Api.DashboardApi
import com.iie.st10089153.txdevsystems_app.repository.DashboardRepository
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DashboardRepository(application.applicationContext)

    private val _dashboardData = MutableLiveData<DashboardItem?>()
    val dashboardData: LiveData<DashboardItem?> = _dashboardData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDashboard(imei: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val data = repository.getCurrentData(imei, forceRefresh)
                _dashboardData.value = data

                if (data == null) {
                    _error.value = "No dashboard data available"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load dashboard: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshDashboard(imei: String) {
        loadDashboard(imei, forceRefresh = true)
    }
}