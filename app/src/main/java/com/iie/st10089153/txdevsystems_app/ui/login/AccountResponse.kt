package com.iie.st10089153.txdevsystems_app.network.Api

import retrofit2.Call
import retrofit2.http.POST

data class AccountResponse(
    val id: Int,
    val username: String,
    val first_name: String,
    val last_name: String,
    val cell: String,
    val email: String,
    val company_name: String?,
    val office_nr: String?,
    val address: String?,
    val user_active: Boolean,
    val Admin: Boolean,
    val comment: String?,
    val timestamp: String,
    val count_logins: Int
)

interface AccountApi {
    @POST("lookup_account/")
    fun getAccount(): Call<AccountResponse>
}
