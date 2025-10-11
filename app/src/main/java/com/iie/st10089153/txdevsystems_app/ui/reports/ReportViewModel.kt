package com.iie.st10089153.txdevsystems_app.ui.reports

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.iie.st10089153.txdevsystems_app.repository.RangeDataRepository
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RangeDataRepository(application.applicationContext)

    private val _reportData = MutableLiveData<List<ReportItem>>()
    val reportData: LiveData<List<ReportItem>> = _reportData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadRangeData(imei: String, startIso: String, stopIso: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val rawData = repository.getRangeData(imei, startIso, stopIso, forceRefresh)

                // Transform RangePoint to ReportItem
                val reportItems = rawData.map { rangePoint ->
                    ReportItem(
                        tempNow = formatTemperature(rangePoint.temp_now),
                        doorStatus = parseDoorStatus(rangePoint.door_status, rangePoint.door_status_bool),
                        powerStatus = parseSupplyStatus(rangePoint.supply_status),
                        batteryStatus = parseBatteryStatus(rangePoint.bat_status),
                        timestamp = rangePoint.timestamp
                    )
                }

                _reportData.value = reportItems

                if (reportItems.isEmpty()) {
                    _error.value = "No data available for selected time range"
                }
            } catch (e: Exception) {
                _error.value = "Failed to load reports: ${e.localizedMessage}"
                _reportData.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatTemperature(temp: Any?): String {
        return when (temp) {
            is Number -> "${temp.toInt()}°"
            is String -> {
                val number = temp.toDoubleOrNull()
                if (number != null) "${number.toInt()}°" else temp
            }
            else -> "--"
        }
    }

    private fun parseDoorStatus(doorStatus: String?, doorStatusBool: String?): DoorStatus {
        return when (doorStatusBool) {
            "0" -> DoorStatus.CLOSED
            "1" -> DoorStatus.OPEN
            else -> DoorStatus.CLOSED
        }
    }

    private fun parseSupplyStatus(supplyStatus: String?): PowerStatus {
        return when (supplyStatus?.lowercase()) {
            "okay", "ok" -> PowerStatus.OK
            else -> PowerStatus.ERROR
        }
    }

    private fun parseBatteryStatus(batteryStatus: String?): BatteryStatus {
        return when (batteryStatus?.lowercase()) {
            "okay", "ok" -> BatteryStatus.OK
            "low" -> BatteryStatus.LOW
            else -> BatteryStatus.ERROR
        }
    }
}