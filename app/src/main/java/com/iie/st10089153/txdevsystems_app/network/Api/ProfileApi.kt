package com.iie.st10089153.txdevsystems_app.network.Api

import com.iie.st10089153.txdevsystems_app.network.AccountResponse
import retrofit2.Call
import retrofit2.http.POST

interface ProfileApi {
    @POST("lookup_account") // Use POST instead of GET
    fun getProfile(): Call<AccountResponse>
}
