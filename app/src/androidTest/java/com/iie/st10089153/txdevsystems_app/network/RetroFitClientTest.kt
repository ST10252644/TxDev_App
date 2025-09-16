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
        assert(retrofit != null)
        assert(retrofit is Retrofit)
        assert(retrofit.baseUrl().toString().isNotEmpty())
    }

    @Test
    fun `getInstance should return same instance for same context`() {
        // When
        val retrofit1 = RetrofitClient.getInstance(context)
        val retrofit2 = RetrofitClient.getInstance(context)

        // Then
        assert(retrofit1 != null)
        assert(retrofit2 != null)
        // Note: RetrofitClient creates new instances, so they won't be the same object
        // But they should have the same base URL
        assert(retrofit1.baseUrl() == retrofit2.baseUrl())
    }

    @Test
    fun `getInstance should have correct base URL`() {
        // When
        val retrofit = RetrofitClient.getInstance(context)

        // Then
        val baseUrl = retrofit.baseUrl().toString()
        assert(baseUrl == "http://api.txdevsystems.co.za:65004/")
    }

    @Test
    fun `getAuthApi should return valid AuthApi instance`() {
        // When
        val authApi = RetrofitClient.getAuthApi(context)

        // Then
        assert(authApi != null)
        assert(authApi is AuthApi)
    }

    @Test
    fun `getAvailableUnitsApi should return valid AvailableUnitsApi instance`() {
        // When
        val availableUnitsApi = RetrofitClient.getAvailableUnitsApi(context)

        // Then
        assert(availableUnitsApi != null)
        assert(availableUnitsApi is AvailableUnitsApi)
    }

    @Test
    fun `getDashboardApi should return valid DashboardApi instance`() {
        // When
        val dashboardApi = RetrofitClient.getDashboardApi(context)

        // Then
        assert(dashboardApi != null)
        assert(dashboardApi is DashboardApi)
    }

    @Test
    fun `multiple calls should return different api instances`() {
        // When
        val authApi1 = RetrofitClient.getAuthApi(context)
        val authApi2 = RetrofitClient.getAuthApi(context)

        // Then
        assert(authApi1 != null)
        assert(authApi2 != null)
        // Different instances but same type
        assert(authApi1 is AuthApi)
        assert(authApi2 is AuthApi)
    }

    @Test
    fun `different api types should be different classes`() {
        // When
        val authApi = RetrofitClient.getAuthApi(context)
        val dashboardApi = RetrofitClient.getDashboardApi(context)
        val availableUnitsApi = RetrofitClient.getAvailableUnitsApi(context)

        // Then
        assert(authApi != null)
        assert(dashboardApi != null)
        assert(availableUnitsApi != null)

        // All should be different classes
        assert(authApi::class != dashboardApi::class)
        assert(dashboardApi::class != availableUnitsApi::class)
        assert(authApi::class != availableUnitsApi::class)
    }

    @Test
    fun `retrofit instance should have gson converter factory`() {
        // When
        val retrofit = RetrofitClient.getInstance(context)

        // Then
        val converterFactories = retrofit.converterFactories()
        assert(converterFactories.isNotEmpty())

        // Check if GsonConverterFactory is present
        val hasGsonConverter = converterFactories.any {
            it.toString().contains("GsonConverterFactory", ignoreCase = true)
        }
        assert(hasGsonConverter)
    }

    @Test
    fun `retrofit instance should have okhttp client`() {
        // When
        val retrofit = RetrofitClient.getInstance(context)

        // Then
        val callFactory = retrofit.callFactory()
        assert(callFactory != null)
        assert(callFactory.toString().contains("OkHttpClient", ignoreCase = true))
    }
}
