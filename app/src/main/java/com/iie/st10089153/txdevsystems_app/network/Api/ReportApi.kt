package com.iie.st10089153.txdevsystems_app.network.Api

import com.iie.st10089153.txdevsystems_app.ui.reports.ColdRoomData
import retrofit2.http.GET

interface ReportApi {
    @GET("coldroom/data")   // ✅ replace with actual Swagger path
    suspend fun getColdRoomData(): List<ColdRoomData>
}