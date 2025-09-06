package com.iie.st10089153.txdevsystems_app.network.Api

import com.iie.st10089153.txdevsystems_app.network.AccountResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST

interface ProfileApi {
    @POST("lookup_account") // endpoint you want
    fun getProfile(): Call<AccountResponse>
}
