package com.iie.st10089153.txdevsystems_app.network.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST


interface AvailableUnitsApi {
    @POST("available_units/")
    fun getAvailableUnits(@Body body: AvailableUnitsRequest): Call<List<AvailableUnit>>
}


data class AvailableUnitsRequest(val status: String)

data class AvailableUnit(
    val imei: String,
    val status: String,
    val name: String,
    val last_seen: String
)
