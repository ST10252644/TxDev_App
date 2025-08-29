package com.iie.st10089153.txdevsystems_app.network

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class TokenInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val sharedPref = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        val requestBuilder = chain.request().newBuilder()

        if (!token.isNullOrEmpty()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        return chain.proceed(requestBuilder.build())
    }
}
