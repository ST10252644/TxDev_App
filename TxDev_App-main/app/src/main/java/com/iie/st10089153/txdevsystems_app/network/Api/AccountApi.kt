package com.iie.st10089153.txdevsystems_app.network.Api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AccountApi {
    @POST("lookup_account/")
    fun lookupAccount(@Body body: LookupAccountRequest): Call<AccountResponse>
}

data class LookupAccountRequest(val lookup: String = "account")

data class AccountResponse(
    val username: String,
    val email: String? = null,
    val account_id: String? = null

)