package com.iie.st10089153.txdevsystems_app.ui.home


import androidx.lifecycle.ViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.repository.UnitsRepository
import kotlinx.coroutines.launch



class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UnitsRepository(application.applicationContext)

    private val _greetingText = MutableLiveData("Hello User")
    val greetingText: LiveData<String> = _greetingText

    private val _subtitleText = MutableLiveData("The following devices are on your account")
    val subtitleText: LiveData<String> = _subtitleText

    private val _devices = MutableLiveData<List<AvailableUnit>>()
    val devices: LiveData<List<AvailableUnit>> = _devices

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadDevices()
    }

    /**
     * Load devices with caching
     * First call loads from cache (instant), then refreshes from API in background
     */
    fun loadDevices(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val units = repository.getUnits(forceRefresh)
                _devices.value = units

                if (units.isEmpty()) {
                    _error.value = "No devices found"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load devices: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshDevices() {
        loadDevices(forceRefresh = true)
    }

    fun updateGreeting(username: String) {
        _greetingText.value = "Hello $username"
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            loadDevices(forceRefresh = true)
        }
    }
}