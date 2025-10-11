package com.iie.st10089153.txdevsystems_app.network.Api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AvailableUnitsApi {
    @POST("available_units/")
    suspend fun getAvailableUnits(@Body body: AvailableUnitsRequest): Response<List<AvailableUnit>>  // ✅ Changed to suspend
}

data class AvailableUnitsRequest(val status: String = "Active")

data class AvailableUnit(
    val imei: String,
    val status: String,
    val name: String,
    val last_seen: String
)