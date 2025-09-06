package com.iie.st10089153.txdevsystems_app.ui.reports

import android.content.Context
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.network.Api.CurrentRequest
import com.iie.st10089153.txdevsystems_app.network.Api.RangeRequest
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem
import com.iie.st10089153.txdevsystems_app.network.Api.RangePoint
import com.iie.st10089153.txdevsystems_app.ui.notifications.Trigger
import com.iie.st10089153.txdevsystems_app.ui.notifications.TriggersRequest
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Repository for reports-related data.
 *
 * Improvements:
 * - fetchRangeExtended now explicitly handles 200 / 204 / 404 / 422 and other HTTP cases.
 * - Temperature parsing is defensive (handles numbers, strings, nulls).
 * - Throws ApiException for 4xx/5xx responses so the ViewModel/UI can surface meaningful messages.
 */
class ReportsRepository(private val context: Context) {

    private val dashboardApi = RetrofitClient.getDashboardApi(context)
    private val rangeApi = RetrofitClient.getRangeApi(context)
    private val notificationsApi = RetrofitClient.getNotificationsApi(context)

    // API exception with status code for better error handling
    class ApiException(
        val statusCode: Int,
        message: String? = null,
        cause: Throwable? = null
    ) : Exception(message ?: "API error: $statusCode", cause)

    // 1) BASIC: current snapshot from txd_cold_room_data
    suspend fun fetchCurrentBasic(imei: String): ColdRoomData? {
        val resp = dashboardApi.getCurrent(CurrentRequest(imei))
        if (!resp.isSuccessful) {
            throw ApiException(resp.code(), "Failed to fetch current data: HTTP ${resp.code()}")
        }
        val d: DashboardItem = resp.body() ?: return null

        // Defensive parsing of temp_now in case backend returns string or number
        val tempNow = d.temp_now?.toString()?.toDoubleOrNull() ?: 0.0

        return ColdRoomData(
            deviceid = d.imei,
            temp_now = tempNow,
            min_temp = null,
            max_temp = null,
            supply_status = null,
            batt_status = null,
            door_status = d.door_status,
            timestamp = d.timestamp
        )
    }

    // 2) EXTENDED list by date range from txd_cold_room_data
    /**
     * Requests the /range/ endpoint with the provided ISO start/stop strings ("yyyy-MM-dd'T'HH:mm:ss").
     *
     * Behavior:
     *  - 200 OK with body -> returns mapped list
     *  - 204 No Content -> returns emptyList()
     *  - 404 -> throws ApiException("Unit not found or not authorized (404)")
     *  - 422 -> throws ApiException("Validation error: <details>")
     *  - other non-2xx -> throws ApiException("API error <code>: <body/message>")
     */
    suspend fun fetchRangeExtended(
        imei: String,
        startIso: String, // "yyyy-MM-dd'T'HH:mm:ss"
        stopIso: String
    ): List<ColdRoomData> {
        val resp = rangeApi.fetchRange(RangeRequest(imei, startIso, stopIso))

        // Explicit handling of various HTTP responses
        if (resp.isSuccessful) {
            // 200 OK (or other 2xx). Note: 204 is a successful response with empty body.
            val rows: List<RangePoint> = resp.body() ?: emptyList()
            return rows.map { r ->
                ColdRoomData(
                    deviceid = r.imei ?: imei,
                    temp_now = (r.temp_now as? Number)?.toDouble()
                        ?: r.temp_now?.toString()?.toDoubleOrNull()
                        ?: 0.0,
                    min_temp = (r.temp_min as? Number)?.toDouble()
                        ?: r.temp_min?.toString()?.toDoubleOrNull(),
                    max_temp = (r.temp_max as? Number)?.toDouble()
                        ?: r.temp_max?.toString()?.toDoubleOrNull(),
                    supply_status = r.supply_status,
                    batt_status = r.bat_status,
                    door_status = r.door_status,
                    timestamp = r.timestamp
                )
            }
        }

        // Non-successful responses (non 2xx)
        when (resp.code()) {
            204 -> {
                // No content for the requested range
                return emptyList()
            }
            404 -> {
                throw ApiException(404, "Unit not found or not authorized")
            }
            422 -> {
                val errBody = resp.errorBody()?.string()
                // The swagger shows { "detail": [ ... ] } — bubble it up for the UI
                throw ApiException(422, errBody ?: "Validation error occurred")
            }
            else -> {
                val errBody = resp.errorBody()?.string()
                val msg = "Unexpected API error: HTTP ${resp.code()} - ${errBody ?: resp.message()}"
                throw ApiException(resp.code(), msg)
            }
        }
    }

    // 3) TRIGGERS table from txd_cold_room_trigger_date
    suspend fun fetchTriggersTable(
        imei: String,
        startDate: String, // "yyyy-MM-dd"
        stopDate: String   // "yyyy-MM-dd"
    ): List<ColdRoomTriggerRow> {
        val req = TriggersRequest(
            imei = imei,
            start = startDate,
            stop = stopDate,
        )
        val resp = notificationsApi.getTriggers(req)

        if (!resp.isSuccessful) {
            when (resp.code()) {
                204 -> {
                    return emptyList()
                }
                404 -> {
                    throw ApiException(404, "Unit not found or not authorized")
                }
                422 -> {
                    val errBody = resp.errorBody()?.string()
                    throw ApiException(422, errBody ?: "Validation error occurred")
                }
                else -> {
                    val errBody = resp.errorBody()?.string()
                    val msg = "Unexpected API error: HTTP ${resp.code()} - ${errBody ?: resp.message()}"
                    throw ApiException(resp.code(), msg)
                }
            }
        }

        val triggers: List<Trigger> = resp.body() ?: emptyList()
        return triggers.map { t ->
            ColdRoomTriggerRow(
                deviceid = t.imei,
                temp_now = t.temp?.toDoubleOrNull(),
                min_temp = t.temp_set_min?.toDoubleOrNull(),
                max_temp = t.temp_set_max?.toDoubleOrNull(),
                timestamp = t.timestamp
            )
        }
    }
}