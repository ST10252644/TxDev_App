package com.iie.st10089153.txdevsystems_app.network.Api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class RangeRequest(
    val imei: String,
    val start: String, // "yyyy-MM-dd'T'HH:mm:ss"
    val stop:  String
)

data class RangePoint(
    val id: Int?,
    val imei: String?,
    val temp_max: Any?,
    val temp_now: Any?,
    val temp_min: Any?,
    val supply_volt: Any?,
    val bat_volt: Any?,
    val supply_status: String?,
    val bat_status: String?,
    val door_status: String?,
    val door_status_bool: String?, // "0"/"1"
    val remaining_data: Any?,
    val signal_strength: Any?,
    val timestamp: String,
    val active: String?
)

interface RangeApi {
    @POST("range/") // NOTE trailing slash
    suspend fun fetchRange(@Body body: RangeRequest): Response<List<RangePoint>>
}
