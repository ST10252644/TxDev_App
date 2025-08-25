package com.iie.st10089153.txdevsystems_app.ui.dashboard_marene

import com.iie.st10089153.txdevsystems_app.network.Api.DashboardApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://api.txdevsystems.co.za:65004"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Create instance for dashboard API
    val instanceDashboard: DashboardApi by lazy {
        retrofit.create(DashboardApi::class.java)
    }
}
