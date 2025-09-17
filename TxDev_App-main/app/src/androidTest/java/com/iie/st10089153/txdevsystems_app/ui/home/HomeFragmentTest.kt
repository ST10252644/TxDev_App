package com.iie.st10089153.txdevsystems_app.ui.home

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsApi
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [28],
    manifest = Config.NONE,
    application = TestApplication::class
)
class SimpleHomeFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var mockAvailableUnitsApi: AvailableUnitsApi
    private lateinit var mockCall: Call<List<AvailableUnit>>
    private lateinit var fragment: HomeFragment

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockAvailableUnitsApi = mockk()
        mockCall = mockk()
        fragment = HomeFragment()

        // Mock RetrofitClient to return our mocked API
        mockkObject(RetrofitClient)
        every { RetrofitClient.getAvailableUnitsApi(any()) } returns mockAvailableUnitsApi
        every { mockAvailableUnitsApi.getAvailableUnits(any()) } returns mockCall
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
        clearAllMocks()
    }

    @Test
    fun `RetrofitClient should be called with correct API request`() {
        // Given
        val mockUnits = listOf(
            AvailableUnit("123456789", "Active", "Device 1", "2024-01-01 12:00:00")
        )

        every { mockCall.enqueue(any()) } answers {
            val callback = firstArg<Callback<List<AvailableUnit>>>()
            callback.onResponse(mockCall, Response.success(mockUnits))
        }

        // When - simulate what happens in onCreateView
        val api = RetrofitClient.getAvailableUnitsApi(context)
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "Active"))

        var receivedUnits: List<AvailableUnit>? = null
        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(call: Call<List<AvailableUnit>>, response: Response<List<AvailableUnit>>) {
                receivedUnits = response.body()
            }
            override fun onFailure(call: Call<List<AvailableUnit>>, t: Throwable) {
                // Handle failure
            }
        })

        // Then
        verify(exactly = 1) { mockAvailableUnitsApi.getAvailableUnits(any()) }
        verify(exactly = 1) {
            mockAvailableUnitsApi.getAvailableUnits(match { it.status == "Active" })
        }
        assert(receivedUnits != null)
        assert(receivedUnits?.size == 1)
        assert(receivedUnits?.first()?.name == "Device 1")
    }

    @Test
    fun `API should handle successful response with multiple devices`() {
        // Given
        val mockUnits = listOf(
            AvailableUnit("123456789", "Active", "Device 1", "2024-01-01 12:00:00"),
            AvailableUnit("987654321", "Active", "Device 2", "2024-01-01 11:30:00"),
            AvailableUnit("555666777", "Active", "Device 3", "2024-01-01 10:00:00")
        )

        every { mockCall.enqueue(any()) } answers {
            val callback = firstArg<Callback<List<AvailableUnit>>>()
            callback.onResponse(mockCall, Response.success(mockUnits))
        }

        // When
        val api = RetrofitClient.getAvailableUnitsApi(context)
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "Active"))

        var receivedUnits: List<AvailableUnit>? = null
        var responseReceived = false

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(call: Call<List<AvailableUnit>>, response: Response<List<AvailableUnit>>) {
                if (response.isSuccessful && response.body() != null) {
                    receivedUnits = response.body()
                    responseReceived = true
                }
            }
            override fun onFailure(call: Call<List<AvailableUnit>>, t: Throwable) {
                responseReceived = true
            }
        })

        // Then
        assert(responseReceived)
        assert(receivedUnits != null)
        assert(receivedUnits?.size == 3)
        assert(receivedUnits?.get(0)?.name == "Device 1")
        assert(receivedUnits?.get(1)?.name == "Device 2")
        assert(receivedUnits?.get(2)?.name == "Device 3")
    }

    @Test
    fun `API should handle empty response`() {
        // Given
        val emptyList = emptyList<AvailableUnit>()

        every { mockCall.enqueue(any()) } answers {
            val callback = firstArg<Callback<List<AvailableUnit>>>()
            callback.onResponse(mockCall, Response.success(emptyList))
        }

        // When
        val api = RetrofitClient.getAvailableUnitsApi(context)
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "Active"))

        var receivedUnits: List<AvailableUnit>? = null
        var responseReceived = false

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(call: Call<List<AvailableUnit>>, response: Response<List<AvailableUnit>>) {
                receivedUnits = response.body()
                responseReceived = true
            }
            override fun onFailure(call: Call<List<AvailableUnit>>, t: Throwable) {
                responseReceived = true
            }
        })

        // Then
        assert(responseReceived)
        assert(receivedUnits != null)
        assert(receivedUnits?.isEmpty() == true)
    }

    @Test
    fun `API should handle error response`() {
        // Given
        every { mockCall.enqueue(any()) } answers {
            val callback = firstArg<Callback<List<AvailableUnit>>>()
            callback.onResponse(mockCall, Response.error(404, mockk(relaxed = true)))
        }

        // When
        val api = RetrofitClient.getAvailableUnitsApi(context)
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "Active"))

        var errorOccurred = false
        var responseReceived = false

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(call: Call<List<AvailableUnit>>, response: Response<List<AvailableUnit>>) {
                if (!response.isSuccessful) {
                    errorOccurred = true
                }
                responseReceived = true
            }
            override fun onFailure(call: Call<List<AvailableUnit>>, t: Throwable) {
                errorOccurred = true
                responseReceived = true
            }
        })

        // Then
        assert(responseReceived)
        assert(errorOccurred)
        verify(exactly = 1) { mockAvailableUnitsApi.getAvailableUnits(any()) }
    }

    @Test
    fun `API should handle network failure`() {
        // Given
        val networkException = Exception("Network connection failed")

        every { mockCall.enqueue(any()) } answers {
            val callback = firstArg<Callback<List<AvailableUnit>>>()
            callback.onFailure(mockCall, networkException)
        }

        // When
        val api = RetrofitClient.getAvailableUnitsApi(context)
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "Active"))

        var failureOccurred = false
        var receivedError: Throwable? = null

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(call: Call<List<AvailableUnit>>, response: Response<List<AvailableUnit>>) {
                // Should not reach here
            }
            override fun onFailure(call: Call<List<AvailableUnit>>, t: Throwable) {
                failureOccurred = true
                receivedError = t
            }
        })

        // Then
        assert(failureOccurred)
        assert(receivedError != null)
        assert(receivedError?.message == "Network connection failed")
        verify(exactly = 1) { mockAvailableUnitsApi.getAvailableUnits(any()) }
    }

    @Test
    fun `DeviceAdapter should be created with device list`() {
        // Given
        val testDevices = listOf(
            AvailableUnit("123456789", "Active", "Device 1", "2024-01-01 12:00:00"),
            AvailableUnit("987654321", "Inactive", "Device 2", "2024-01-01 11:30:00")
        )

        // When
        val adapter = DeviceAdapter(testDevices)

        // Then
        assert(adapter != null)
        assert(adapter.itemCount == 2)
    }

    @Test
    fun `DeviceAdapter should handle empty device list`() {
        // Given
        val emptyDevices = emptyList<AvailableUnit>()

        // When
        val adapter = DeviceAdapter(emptyDevices)

        // Then
        assert(adapter != null)
        assert(adapter.itemCount == 0)
    }

    @Test
    fun `AvailableUnitsRequest should be created with correct status`() {
        // Given
        val status = "Active"

        // When
        val request = AvailableUnitsRequest(status)

        // Then
        assert(request.status == "Active")
    }

    @Test
    fun `AvailableUnit should contain correct data`() {
        // Given
        val imei = "123456789"
        val status = "Active"
        val name = "Test Device"
        val lastSeen = "2024-01-01 12:00:00"

        // When
        val unit = AvailableUnit(imei, status, name, lastSeen)

        // Then
        assert(unit.imei == imei)
        assert(unit.status == status)
        assert(unit.name == name)
        assert(unit.last_seen == lastSeen)
    }
}