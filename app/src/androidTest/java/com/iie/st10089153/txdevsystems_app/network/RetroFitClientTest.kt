package com.iie.st10089153.txdevsystems_app.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.txdevsystems_app.network.Api.AuthApi
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsApi
import com.iie.st10089153.txdevsystems_app.network.Api.DashboardApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Retrofit

@RunWith(AndroidJUnit4::class)
class RetrofitClientTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun getInstance_returnsValidRetrofit() {
        val retrofit = RetrofitClient.getInstance(context)
        // Type sanity
        assertTrue("Instance is not Retrofit", retrofit is Retrofit)
        // Base URL should exist
        assertTrue("Base URL should not be empty", retrofit.baseUrl().toString().isNotEmpty())
    }

    @Test
    fun getInstance_sameContext_sameBaseUrl() {
        val retrofit1 = RetrofitClient.getInstance(context)
        val retrofit2 = RetrofitClient.getInstance(context)
        assertEquals(
            "Base URLs should match for the same context",
            retrofit1.baseUrl(),
            retrofit2.baseUrl()
        )
    }

    @Test
    fun getInstance_hasExpectedBaseUrl() {
        val retrofit = RetrofitClient.getInstance(context)
        assertEquals("http://api.txdevsystems.co.za:65004/", retrofit.baseUrl().toString())
    }

    @Test
    fun getAuthApi_returnsProxyImplementingAuthApi() {
        val api = RetrofitClient.getAuthApi(context)
        // Retrofit returns a dynamic proxy that implements the interface
        assertTrue(
            "Returned object must implement AuthApi",
            api.javaClass.interfaces.contains(AuthApi::class.java)
        )
    }

    @Test
    fun getAvailableUnitsApi_returnsProxyImplementingAvailableUnitsApi() {
        val api = RetrofitClient.getAvailableUnitsApi(context)
        assertTrue(
            "Returned object must implement AvailableUnitsApi",
            api.javaClass.interfaces.contains(AvailableUnitsApi::class.java)
        )
    }

    @Test
    fun getDashboardApi_returnsProxyImplementingDashboardApi() {
        val api = RetrofitClient.getDashboardApi(context)
        assertTrue(
            "Returned object must implement DashboardApi",
            api.javaClass.interfaces.contains(DashboardApi::class.java)
        )
    }

    @Test
    fun differentApis_implementDifferentInterfaces() {
        val authApi = RetrofitClient.getAuthApi(context)
        val dashApi = RetrofitClient.getDashboardApi(context)
        val availApi = RetrofitClient.getAvailableUnitsApi(context)

        val authIfaces = authApi.javaClass.interfaces.toSet()
        val dashIfaces = dashApi.javaClass.interfaces.toSet()
        val availIfaces = availApi.javaClass.interfaces.toSet()

        assertTrue("Auth vs Dashboard should differ", authIfaces != dashIfaces)
        assertTrue("Dashboard vs Available should differ", dashIfaces != availIfaces)
        assertTrue("Auth vs Available should differ", authIfaces != availIfaces)
    }

    @Test
    fun retrofit_hasGsonConverterFactory() {
        val retrofit = RetrofitClient.getInstance(context)
        val converterFactories = retrofit.converterFactories()
        assertTrue("No converter factories installed", converterFactories.isNotEmpty())

        val hasGson = converterFactories.any {
            it.toString().contains("GsonConverterFactory", ignoreCase = true)
        }
        assertTrue("GsonConverterFactory not found on Retrofit", hasGson)
    }

    @Test
    fun retrofit_usesOkHttpClient() {
        val retrofit = RetrofitClient.getInstance(context)
        val callFactory = retrofit.callFactory()
        assertTrue(
            "CallFactory should be OkHttpClient",
            callFactory.toString().contains("OkHttpClient", ignoreCase = true)
        )
    }
}
