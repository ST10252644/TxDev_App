package com.iie.st10089153.txdevsystems_app.ui.login

import retrofit2.Call
import retrofit2.http.*

interface AuthApi {
    @FormUrlEncoded
    @POST("token")
    fun login(
        @Field("grant_type") grantType: String = "password",
        @Field("username") username: String,
        @Field("password") password: String
    ): Call<LoginResponse>
}