package com.iie.st10089153.txdevsystems_app.ui.chart

import android.content.Context
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint
import com.iie.st10089153.txdevsystems_app.network.Api.RangeRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object ChartsRepository {

    // API expects: "yyyy-MM-dd'T'HH:mm:ss"
    private val SIMPLE_ISO: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

    /** 00:00:00 (assumed UTC on server side) */
    private fun startIso(day: LocalDate): String =
        SIMPLE_ISO.format(day.atStartOfDay())

    /** 23:59:59 (assumed UTC on server side) */
    private fun endIso(day: LocalDate): String =
        SIMPLE_ISO.format(day.atTime(LocalTime.of(23, 59, 59)))

    suspend fun load(
        ctx: Context,
        imei: String,
        day: LocalDate,
        window: RangeWindow
    ): List<RangePoint> {
        val (start, stop) = when (window) {
            RangeWindow.DAY -> startIso(day) to endIso(day)
            RangeWindow.WEEK -> {
                val startDay = day.minusDays(6)
                startIso(startDay) to endIso(day)
            }
            RangeWindow.MONTH -> {
                val startDay = day.minusDays(29)
                startIso(startDay) to endIso(day)
            }
            RangeWindow.CUSTOM -> error("Use loadExplicit for CUSTOM")
        }
        return loadExplicit(ctx, imei, start, stop)
    }

    suspend fun loadExplicit(
        ctx: Context,
        imei: String,
        startIso: String,
        stopIso: String
    ): List<RangePoint> {
        val api = RetrofitClient.getRangeApi(ctx)
        // Match your interface exactly: fetchRange(...)
        val resp = api.fetchRange(RangeRequest(imei = imei, start = startIso, stop = stopIso))
        if (resp.isSuccessful) {
            return resp.body() ?: emptyList()
        } else {
            throw IllegalStateException("Range API ${resp.code()} ${resp.errorBody()?.string()}")
        }
    }
}
