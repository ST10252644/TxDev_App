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

    @POST("user/fcm-token")
    suspend fun registerFcmToken(
        @Body request: FcmTokenRequest
    ): Response<FcmTokenResponse>
}

data class FcmTokenRequest(
    val token: String,
    val device_type: String = "android"
)

data class FcmTokenResponse(
    val success: Boolean,
    val message: String?
)