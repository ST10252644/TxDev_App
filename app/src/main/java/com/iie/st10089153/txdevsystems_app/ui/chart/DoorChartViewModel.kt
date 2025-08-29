package com.iie.st10089153.txdevsystems_app.ui.chart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DoorUi(
    val loading: Boolean = false,
    val error: String? = null,
    val points: List<RangePoint> = emptyList(),
    val day: LocalDate = LocalDate.now(),
    val window: RangeWindow = RangeWindow.MONTH,
    val startIso: String? = null,
    val stopIso: String? = null
)

class DoorChartViewModel(private val repo: ChartsRepository = ChartsRepository()) : ViewModel() {
    private val _ui = MutableStateFlow(DoorUi())
    val ui: StateFlow<DoorUi> = _ui

    fun fetch(context: Context, imei: String, day: LocalDate, window: RangeWindow) {
        _ui.value = _ui.value.copy(loading = true, error = null, day = day, window = window, startIso = null, stopIso = null)
        viewModelScope.launch {
            try {
                val data = repo.load(context, imei, day, window)
                _ui.value = _ui.value.copy(loading = false, points = data)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message, points = emptyList())
            }
        }
    }

    fun fetchRange(context: Context, imei: String, startIso: String, stopIso: String) {
        _ui.value = _ui.value.copy(loading = true, error = null, window = RangeWindow.CUSTOM, startIso = startIso, stopIso = stopIso)
        viewModelScope.launch {
            try {
                val data = repo.loadExplicit(context, imei, startIso, stopIso)
                _ui.value = _ui.value.copy(loading = false, points = data)
            } catch (e: Exception) {
                _ui.value = _ui.value.copy(loading = false, error = e.message, points = emptyList())
            }
        }
    }
}
