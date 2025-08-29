package com.iie.st10089153.txdevsystems_app.ui.chart

import android.content.Context
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint
import com.iie.st10089153.txdevsystems_app.network.Api.RangeRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

enum class RangeWindow { DAY, WEEK, MONTH, CUSTOM }

class ChartsRepository {
    private val outFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    private fun bounds(anchor: LocalDate, win: RangeWindow): Pair<String, String> {
        return when (win) {
            RangeWindow.DAY -> {
                val s = anchor.atStartOfDay()
                val e = anchor.atTime(LocalTime.MAX).withNano(0)
                s.format(outFmt) to e.format(outFmt)
            }
            RangeWindow.WEEK -> {
                val monday = anchor.minusDays(((anchor.dayOfWeek.value + 6) % 7).toLong())
                val s = monday.atStartOfDay()
                val e = monday.plusDays(6).atTime(LocalTime.MAX).withNano(0)
                s.format(outFmt) to e.format(outFmt)
            }
            RangeWindow.MONTH -> {
                val first = anchor.withDayOfMonth(1)
                val last = first.plusMonths(1).minusDays(1)
                val s = first.atStartOfDay()
                val e = last.atTime(LocalTime.MAX).withNano(0)
                s.format(outFmt) to e.format(outFmt)
            }
            RangeWindow.CUSTOM -> throw IllegalArgumentException("Use loadExplicit for CUSTOM")
        }
    }

    suspend fun load(context: Context, imei: String, day: LocalDate, win: RangeWindow)
            : List<RangePoint> = withContext(Dispatchers.IO) {
        val api = RetrofitClient.getRangeApi(context)
        val (start, stop) = bounds(day, win)
        val resp = api.fetchRange(RangeRequest(imei, start, stop))
        if (resp.isSuccessful) resp.body().orEmpty()
        else throw Exception("HTTP ${resp.code()}: ${resp.errorBody()?.string() ?: resp.message()}")
    }

    // NEW: explicit range (start/stop ISO)
    suspend fun loadExplicit(context: Context, imei: String, startIso: String, stopIso: String)
            : List<RangePoint> = withContext(Dispatchers.IO) {
        val api = RetrofitClient.getRangeApi(context)
        val resp = api.fetchRange(RangeRequest(imei, startIso, stopIso))
        if (resp.isSuccessful) resp.body().orEmpty()
        else throw Exception("HTTP ${resp.code()}: ${resp.errorBody()?.string() ?: resp.message()}")
    }
}
