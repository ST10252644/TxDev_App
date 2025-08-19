package com.iie.st10089153.txdevsystems_app.ui.home

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("available_units/")
    fun getAvailableUnits(
        @Query("status") status: String = "Active"
    ): Call<List<UnitResponse>>
}