package com.iie.st10089153.txdevsystems_app.network.Api

import com.iie.st10089153.txdevsystems_app.network.AccountResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

interface ProfileApi {
    @POST("lookup_account") // Use POST instead of GET
    fun getProfile(): Call<AccountResponse>

    @PUT("edit_account/")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UpdateProfileResponse>
}

data class UpdateProfileRequest(
    val address: String,
    val first_name: String
)

// Response model for update
data class UpdateProfileResponse(
    val address: String?,
    val details: String?,
    val first_name: String?
)