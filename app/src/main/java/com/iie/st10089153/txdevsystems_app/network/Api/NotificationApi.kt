package com.iie.st10089153.txdevsystems_app.network.Api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationsApi {

    @POST("available_units/")
    suspend fun getAvailableUnits(
        @Body request: AvailableUnitsRequest  // ✅ From same package
    ): Response<List<AvailableUnit>>

    @POST("triggers/")
    suspend fun getTriggers(
        @Body request: TriggersRequest  // ✅ From same package
    ): Response<List<Trigger>>
}