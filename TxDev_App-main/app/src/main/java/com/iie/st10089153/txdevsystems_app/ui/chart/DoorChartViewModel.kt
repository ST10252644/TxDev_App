package com.iie.st10089153.txdevsystems_app.ui.chart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class DoorChartViewModel : ViewModel() {
    private val _ui = MutableStateFlow(ChartUi())
    val ui = _ui.asStateFlow()

    fun fetch(ctx: Context, imei: String, day: LocalDate, window: RangeWindow) {
        viewModelScope.launch {
            try {
                val pts = ChartsRepository.load(ctx, imei, day, window)
                _ui.value = ChartUi(day = day, window = window, points = pts)
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: "Unknown error")
            }
        }
    }

    fun fetchRange(ctx: Context, imei: String, startIso: String, stopIso: String) {
        viewModelScope.launch {
            try {
                val pts = ChartsRepository.loadExplicit(ctx, imei, startIso, stopIso)
                _ui.value = ChartUi(
                    day = _ui.value.day,
                    window = RangeWindow.CUSTOM,
                    points = pts,
                    startIso = startIso,
                    stopIso = stopIso
                )
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(error = t.message ?: "Unknown error")
            }
        }
    }
}
