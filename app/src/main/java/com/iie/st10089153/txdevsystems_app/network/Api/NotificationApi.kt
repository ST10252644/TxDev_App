package com.iie.st10089153.txdevsystems_app.network.Api


import com.iie.st10089153.txdevsystems_app.ui.notifications.AvailableUnitsRequest
import com.iie.st10089153.txdevsystems_app.ui.notifications.Trigger
import com.iie.st10089153.txdevsystems_app.ui.notifications.TriggersRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationsApi {

    @POST("available_units/")
    suspend fun getAvailableUnits(
        @Body request: AvailableUnitsRequest
    ): Response<List<AvailableUnit>>

    @POST("triggers/")
    suspend fun getTriggers(
        @Body request: TriggersRequest
    ): Response<List<Trigger>>
}