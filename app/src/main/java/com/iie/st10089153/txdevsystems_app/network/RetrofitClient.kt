package com.iie.st10089153.txdevsystems_app.network

import android.content.Context
import com.iie.st10089153.txdevsystems_app.network.Api.AuthApi
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsApi
import com.iie.st10089153.txdevsystems_app.network.Api.DashboardApi
import com.iie.st10089153.txdevsystems_app.network.Api.NotificationsApi
import com.iie.st10089153.txdevsystems_app.network.Api.RangeApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitClient {
   //private const val BASE_URL = "http://192.168.5.5:30082/" //just Cherika use this
    private const val BASE_URL = "http://api.txdevsystems.co.za:65004/" //the rest use this one



    fun getInstance(context: Context): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(TokenInterceptor(context)) // attach token automatically
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    //add api calls here
    fun getAuthApi(context: Context): AuthApi {
        return getInstance(context).create(AuthApi::class.java)
    }

    fun getAvailableUnitsApi(context: Context): AvailableUnitsApi {
        return getInstance(context).create(AvailableUnitsApi::class.java)
    }

    fun getDashboardApi(context: Context): DashboardApi {
        return getInstance(context).create(DashboardApi::class.java)
    }



    fun getRangeApi(context: Context): RangeApi =
        getInstance(context).create(RangeApi::class.java)

    fun getNotificationsApi(context: Context): NotificationsApi =
        getInstance(context).create(NotificationsApi::class.java)




}
