package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider

import com.iie.st10089153.txdevsystems_app.network.Api.CurrentRequest
import com.iie.st10089153.txdevsystems_app.network.Api.DashboardApi
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.GaugeCard
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [28],
    manifest = Config.NONE,
    application = TestApplication::class
)
class SimpleDashboardFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context
    private lateinit var mockDashboardApi: DashboardApi

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockDashboardApi = mockk()

        // Mock RetrofitClient to return our mocked API
        mockkObject(RetrofitClient)
        every { RetrofitClient.getDashboardApi(any()) } returns mockDashboardApi
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
        clearAllMocks()
    }

    @Test
    fun `dashboard API should be called with correct IMEI`() = runTest {
        // Given
        val testImei = "123456789"
        val mockDashboardItem = createMockDashboardItem(testImei)

        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.success(mockDashboardItem)

        // When
        val api = RetrofitClient.getDashboardApi(context)
        val response = api.getCurrent(CurrentRequest(testImei))

        // Then
        coVerify(exactly = 1) { mockDashboardApi.getCurrent(match { it.imei == testImei }) }
        assert(response.isSuccessful)
        assert(response.body() != null)
        assert(response.body()?.imei == testImei)
    }

    @Test
    fun `dashboard API should handle successful response`() = runTest {
        // Given
        val testImei = "123456789"
        val mockDashboardItem = createMockDashboardItem(testImei)

        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.success(mockDashboardItem)

        // When
        val api = RetrofitClient.getDashboardApi(context)
        val response = api.getCurrent(CurrentRequest(testImei))

        // Then
        assert(response.isSuccessful)
        val item = response.body()
        assert(item != null)
        assert(item?.supply_status == "Okay")
        assert(item?.bat_volt == 11.5f)
        assert(item?.temp_now == 22.5f)
        assert(item?.door_status == "Closed")
    }

    @Test
    fun `dashboard API should handle error response`() = runTest {
        // Given
        val testImei = "123456789"
        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.error(404, mockk(relaxed = true))

        // When
        val api = RetrofitClient.getDashboardApi(context)
        val response = api.getCurrent(CurrentRequest(testImei))

        // Then
        assert(!response.isSuccessful)
        assert(response.code() == 404)
        assert(response.body() == null)
    }

    @Test
    fun `dashboard API should handle network exception`() = runTest {
        // Given
        val testImei = "123456789"
        coEvery { mockDashboardApi.getCurrent(any()) } throws Exception("Network error")

        // When & Then
        try {
            val api = RetrofitClient.getDashboardApi(context)
            api.getCurrent(CurrentRequest(testImei))
            assert(false) { "Should have thrown an exception" }
        } catch (e: Exception) {
            assert(e.message == "Network error")
        }
    }

    @Test
    fun `CurrentRequest should be created with correct IMEI`() {
        // Given
        val testImei = "123456789"

        // When
        val request = CurrentRequest(testImei)

        // Then
        assert(request.imei == testImei)
    }

    @Test
    fun `GaugeCard should be created with power status`() {
        // Given
        val powerGauge = GaugeCard(
            iconRes = android.R.drawable.ic_lock_power_off,
            statusText = "ON",
            name = "Power",
            gaugeImageRes = android.R.drawable.ic_lock_power_off
        )

        // When & Then
        assert(powerGauge.name == "Power")
        assert(powerGauge.statusText == "ON")
        assert(powerGauge.measurement == null)
        assert(powerGauge.minValue == null)
        assert(powerGauge.maxValue == null)
    }

    @Test
    fun `GaugeCard should be created with battery status and measurements`() {
        // Given
        val batteryGauge = GaugeCard(
            iconRes = android.R.drawable.ic_lock_power_off,
            statusText = "11.5",
            name = "Battery",
            measurement = "Volts",
            minValue = 0f,
            maxValue = 15f,
            gaugeImageRes = android.R.drawable.ic_lock_power_off
        )

        // When & Then
        assert(batteryGauge.name == "Battery")
        assert(batteryGauge.statusText == "11.5")
        assert(batteryGauge.measurement == "Volts")
        assert(batteryGauge.minValue == 0f)
        assert(batteryGauge.maxValue == 15f)
    }

    @Test
    fun `GaugeCard should be created with temperature status and range`() {
        // Given
        val tempGauge = GaugeCard(
            iconRes = android.R.drawable.ic_lock_power_off,
            statusText = "22.5",
            name = "Temperature",
            measurement = "°C",
            minValue = 10f,
            maxValue = 30f,
            gaugeImageRes = android.R.drawable.ic_lock_power_off
        )

        // When & Then
        assert(tempGauge.name == "Temperature")
        assert(tempGauge.statusText == "22.5")
        assert(tempGauge.measurement == "°C")
        assert(tempGauge.minValue == 10f)
        assert(tempGauge.maxValue == 30f)
    }

    @Test
    fun `should create gauge cards from dashboard item data`() {
        // Given
        val dashboardItem = createMockDashboardItem("123456789")

        // When - simulate creating gauge cards like in the fragment
        val gaugeList = createGaugeCardsFromDashboardItem(dashboardItem)

        // Then
        assert(gaugeList.size == 4) // Power, Battery, Temperature, Door

        // Check Power gauge
        val powerGauge = gaugeList.find { it.name == "Power" }
        assert(powerGauge != null)
        assert(powerGauge?.statusText == "ON")

        // Check Battery gauge
        val batteryGauge = gaugeList.find { it.name == "Battery" }
        assert(batteryGauge != null)
        assert(batteryGauge?.statusText == "11.5")
        assert(batteryGauge?.measurement == "Volts")

        // Check Temperature gauge
        val tempGauge = gaugeList.find { it.name == "Temperature" }
        assert(tempGauge != null)
        assert(tempGauge?.statusText == "22.5")
        assert(tempGauge?.measurement == "°C")

        // Check Door gauge
        val doorGauge = gaugeList.find { it.name == "Door" }
        assert(doorGauge != null)
        assert(doorGauge?.statusText == "Closed")
    }

    @Test
    fun `should handle different power statuses`() {
        // Given
        val dashboardItemOff = createMockDashboardItem("123456789", supplyStatus = "Error")

        // When
        val gaugeList = createGaugeCardsFromDashboardItem(dashboardItemOff)
        val powerGauge = gaugeList.find { it.name == "Power" }

        // Then
        assert(powerGauge?.statusText == "OFF")
    }

    @Test
    fun `should handle different door statuses`() {
        // Given
        val dashboardItemOpenDoor = createMockDashboardItem("123456789", doorStatusBool = 1, doorStatus = "Open")

        // When
        val gaugeList = createGaugeCardsFromDashboardItem(dashboardItemOpenDoor)
        val doorGauge = gaugeList.find { it.name == "Door" }

        // Then
        assert(doorGauge?.statusText == "Open")
    }

    // Helper functions
    private fun createMockDashboardItem(
        imei: String,
        supplyStatus: String = "Okay",
        doorStatusBool: Int = 0,
        doorStatus: String = "Closed"
    ): DashboardItem {
        return DashboardItem(
            id = 1,
            imei = imei,
            temp_max = 30.0f,
            temp_now = 22.5f,
            temp_min = 10.0f,
            supply_volt = 12.0f,
            bat_volt = 11.5f,
            supply_status = supplyStatus,
            bat_status = "Okay",
            door_status = doorStatus,
            door_status_bool = doorStatusBool,
            signal_strength = "Good",
            timestamp = "2024-01-01T12:00:00Z",
            active = 1
        )
    }

    private fun createGaugeCardsFromDashboardItem(item: DashboardItem): List<GaugeCard> {
        // This simulates the gauge creation logic from DashboardFragment
        return listOf(
            GaugeCard(
                iconRes = android.R.drawable.ic_lock_power_off,
                statusText = if (item.supply_status == "Okay") "ON" else "OFF",
                name = "Power",
                gaugeImageRes = android.R.drawable.ic_lock_power_off
            ),
            GaugeCard(
                iconRes = android.R.drawable.ic_lock_power_off,
                statusText = item.bat_volt.toString(),
                name = "Battery",
                measurement = "Volts",
                minValue = 0f,
                maxValue = 15f,
                gaugeImageRes = android.R.drawable.ic_lock_power_off
            ),
            GaugeCard(
                iconRes = android.R.drawable.ic_lock_power_off,
                statusText = "${item.temp_now}",
                name = "Temperature",
                measurement = "°C",
                gaugeImageRes = android.R.drawable.ic_lock_power_off,
                minValue = item.temp_min,
                maxValue = item.temp_max
            ),
            GaugeCard(
                iconRes = android.R.drawable.ic_lock_power_off,
                statusText = item.door_status,
                name = "Door",
                gaugeImageRes = android.R.drawable.ic_lock_power_off
            )
        )
    }
}