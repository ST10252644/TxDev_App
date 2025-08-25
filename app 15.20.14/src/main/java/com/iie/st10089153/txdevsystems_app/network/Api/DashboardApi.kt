package com.iie.st10089153.txdevsystems_app.network.Api

import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class CurrentRequest(
    val imei: String
)

interface DashboardApi {
    @POST("current/")
    suspend fun getCurrent(@Body request: CurrentRequest): Response<DashboardItem>
}


//import retrofit2.Response
//import retrofit2.http.POST
//import retrofit2.http.Path
//


//
//Marene's code
//interface DashboardApi {
//    @POST("txd_cold_room_data/current/{imei}")
//    suspend fun getDashboardItem(
//        @Path("imei") imei: String
//    ): Response<dashboard_marene.models.DashboardItem>
//}


