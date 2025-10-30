package com.iie.st10089153.txdevsystems_app.ui.reports

import android.content.Context
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.network.Api.RangeRequest
import java.text.SimpleDateFormat
import java.util.*

class ReportsRepository(private val context: Context) {

    private val rangeApi = RetrofitClient.getRangeApi(context)

    class ApiException(
        val statusCode: Int,
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message ?: "API error: $statusCode", cause)

    suspend fun fetchRangeData(
        imei: String,
        startIso: String,
        stopIso: String
    ): List<ReportItem> {
        val resp = rangeApi.fetchRange(RangeRequest(imei, startIso, stopIso))

        if (!resp.isSuccessful) {
            when (resp.code()) {
                204 -> return emptyList()
                404 -> throw ApiException(404, "Unit not found or not authorized")
                422 -> {
                    val errBody = resp.errorBody()?.string()
                    throw ApiException(422, errBody ?: "Validation error occurred")
                }
                else -> {
                    val errBody = resp.errorBody()?.string()
                    throw ApiException(resp.code(), errBody ?: "API error occurred")
                }
            }
        }

        val rawData = resp.body() ?: return emptyList()

        return rawData.map { item ->
            ReportItem(
                tempNow = formatTemperature(item.temp_now),
                doorStatus = parseDoorStatus(item.door_status, item.door_status_bool),
                powerStatus = parseSupplyStatus(item.supply_status),
                batteryStatus = parseBatteryStatus(item.bat_status),
                timestamp = formatTimestamp(item.timestamp)
            )
        }.sortedByDescending { it.timestamp } // Most recent first
    }

    private fun formatTemperature(temp: Any?): String {
        return when (temp) {
            is Number -> "${temp.toInt()}°"
            is String -> {
                val cleaned = temp.trim().replace("+", "").replace(" ", "")
                val number = cleaned.toDoubleOrNull()
                if (number != null) "${number.toInt()}°" else cleaned
            }
            else -> "--"
        }
    }

    private fun parseDoorStatus(doorStatus: String?, doorStatusBool: String?): DoorStatus {
        return when {
            doorStatusBool == "0" || doorStatus?.lowercase()?.contains("closed") == true -> DoorStatus.CLOSED
            doorStatusBool == "1" || doorStatus?.lowercase()?.contains("open") == true -> DoorStatus.OPEN
            else -> DoorStatus.CLOSED // Default to closed
        }
    }

    private fun parseSupplyStatus(supplyStatus: String?): PowerStatus {
        return when (supplyStatus?.lowercase()) {
            "okay", "ok", "good", "normal" -> PowerStatus.OK
            else -> PowerStatus.ERROR
        }
    }

    private fun parseBatteryStatus(batteryStatus: String?): BatteryStatus {
        return when (batteryStatus?.lowercase()) {
            "okay", "ok", "good", "normal" -> BatteryStatus.OK
            "low", "warning" -> BatteryStatus.LOW
            else -> BatteryStatus.ERROR
        }
    }

    private fun formatTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return "--:--"

        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)
            return date?.let { outputFormat.format(it) } ?: timestamp
        } catch (e: Exception) {
            return timestamp
        }
    }
}