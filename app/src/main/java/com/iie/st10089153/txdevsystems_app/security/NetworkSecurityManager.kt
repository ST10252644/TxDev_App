// app/src/main/java/com/iie/st10089153/txdevsystems_app/security/NetworkSecurityManager.kt
package com.iie.st10089153.txdevsystems_app.security

import android.content.Context
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class NetworkSecurityManager(private val context: Context) {

    fun createSecureOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Always use basic logging for development, none for production
            level = if (isDebugMode()) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor())
            .addInterceptor(securityHeadersInterceptor())
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            // Note: Certificate pinning disabled for local development
            // Enable for production with proper HTTPS endpoints
            .build()
    }

    private fun authInterceptor() = Interceptor { chain ->
        val original = chain.request()
        val token = SecureTokenManager.getInstance(context).getAuthToken()

        val requestBuilder = original.newBuilder()
            .method(original.method, original.body)

        // Add auth token if available
        token?.let {
            requestBuilder.header("Authorization", "Bearer $it")
        }

        // Add security headers
        requestBuilder
            .header("User-Agent", "TMS-Android/${getAppVersion()}")
            .header("X-Requested-With", "XMLHttpRequest")

        chain.proceed(requestBuilder.build())
    }

    private fun securityHeadersInterceptor() = Interceptor { chain ->
        val request = chain.request()
        val response = chain.proceed(request)

        // Log security warnings for development
        if (isDebugMode()) {
            if (request.url.scheme == "http") {
                android.util.Log.w("NetworkSecurity",
                    "WARNING: Using HTTP connection to ${request.url.host}. " +
                            "Consider using HTTPS in production.")
            }
        }

        response
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    private fun isDebugMode(): Boolean {
        return try {
            val applicationInfo = context.applicationInfo
            (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (e: Exception) {
            false
        }
    }
}