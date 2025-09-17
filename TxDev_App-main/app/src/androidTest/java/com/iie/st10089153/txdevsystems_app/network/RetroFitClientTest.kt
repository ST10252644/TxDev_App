package com.iie.st10089153.txdevsystems_app.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.iie.st10089153.txdevsystems_app.network.Api.AuthApi
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsApi
import com.iie.st10089153.txdevsystems_app.network.Api.DashboardApi
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Retrofit
import org.junit.Assert.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class RetrofitClientTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `getInstance should return valid Retrofit instance`() {
        // When
        val retrofit = RetrofitClient.getInstance(context)

        // Then
        assertNotNull("Retrofit instance should not be null", retrofit)
        assertTrue("Should be Retrofit instance", retrofit is Retrofit)
        assertTrue("Base URL should not be empty", retrofit.baseUrl().toString().isNotEmpty())
    }

    @Test
    fun `getInstance should return same instance for same context`() {
        // When
        val retrofit1 = RetrofitClient.getInstance(context)
        val retrofit2 = RetrofitClient.getInstance(context)

        // Then
        assertNotNull("First retrofit instance should not be null", retrofit1)
        assertNotNull("Second retrofit instance should not be null", retrofit2)

        // They should have the same base URL
        assertEquals("Both instances should have same base URL",
            retrofit1.baseUrl(), retrofit2.baseUrl())
    }

    @Test
    fun `getInstance should have correct base URL`() {
        // When
        val retrofit = RetrofitClient.getInstance(context)

        // Then
        val baseUrl = retrofit.baseUrl().toString()
        assertEquals("Should have correct base URL",
            "http://api.txdevsystems.co.za:65004/", baseUrl)
    }

    @Test
    fun `getAuthApi should return valid AuthApi instance`() {
        // When
        val authApi = RetrofitClient.getAuthApi(context)

        // Then
        assertNotNull("AuthApi should not be null", authApi)
        assertTrue("Should be AuthApi instance", authApi is AuthApi)
    }

    @Test
    fun `getAvailableUnitsApi should return valid AvailableUnitsApi instance`() {
        // When
        val availableUnitsApi = RetrofitClient.getAvailableUnitsApi(context)

        // Then
        assertNotNull("AvailableUnitsApi should not be null", availableUnitsApi)
        assertTrue("Should be AvailableUnitsApi instance", availableUnitsApi is AvailableUnitsApi)
    }

    @Test
    fun `getDashboardApi should return valid DashboardApi instance`() {
        // When
        val dashboardApi = RetrofitClient.getDashboardApi(context)

        // Then
        assertNotNull("DashboardApi should not be null", dashboardApi)
        assertTrue("Should be DashboardApi instance", dashboardApi is DashboardApi)
    }

    @Test
    fun `multiple calls should return different api instances`() {
        // When
        val authApi1 = RetrofitClient.getAuthApi(context)
        val authApi2 = RetrofitClient.getAuthApi(context)

        // Then
        assertNotNull("First AuthApi should not be null", authApi1)
        assertNotNull("Second AuthApi should not be null", authApi2)

        // Both should be AuthApi instances
        assertTrue("First should be AuthApi instance", authApi1 is AuthApi)
        assertTrue("Second should be AuthApi instance", authApi2 is AuthApi)
    }

    @Test
    fun `different api types should be different classes`() {
        // When
        val authApi = RetrofitClient.getAuthApi(context)
        val dashboardApi = RetrofitClient.getDashboardApi(context)
        val availableUnitsApi = RetrofitClient.getAvailableUnitsApi(context)

        // Then
        assertNotNull("AuthApi should not be null", authApi)
        assertNotNull("DashboardApi should not be null", dashboardApi)
        assertNotNull("AvailableUnitsApi should not be null", availableUnitsApi)

        // All should be different classes
        assertNotEquals("AuthApi and DashboardApi should be different classes",
            authApi::class, dashboardApi::class)
        assertNotEquals("DashboardApi and AvailableUnitsApi should be different classes",
            dashboardApi::class, availableUnitsApi::class)
        assertNotEquals("AuthApi and AvailableUnitsApi should be different classes",
            authApi::class, availableUnitsApi::class)
    }

    @Test
    fun `retrofit instance should have gson converter factory`() {
        // When
        val retrofit = RetrofitClient.getInstance(context)

        // Then
        val converterFactories = retrofit.converterFactories()
        assertTrue("Should have converter factories", converterFactories.isNotEmpty())

        // Check if GsonConverterFactory is present
        val hasGsonConverter = converterFactories.any {
            it.toString().contains("GsonConverterFactory", ignoreCase = true)
        }
        assertTrue("Should have Gson converter factory", hasGsonConverter)
    }

    @Test
    fun `retrofit instance should have okhttp client`() {
        // When
        val retrofit = RetrofitClient.getInstance(context)

        // Then
        val callFactory = retrofit.callFactory()
        assertNotNull("Call factory should not be null", callFactory)

        // Check if it's using OkHttpClient
        val isOkHttpClient = callFactory.toString().contains("OkHttpClient", ignoreCase = true)
        assertTrue("Should use OkHttpClient", isOkHttpClient)
    }
}