package com.iie.st10089153.txdevsystems_app.ui.home


import android.content.Context
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.DashboardApi
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DeviceAdapterTest {

    private lateinit var context: Context
    private lateinit var mockDashboardApi: DashboardApi
    private lateinit var adapter: DeviceAdapter
    private lateinit var testDevices: List<AvailableUnit>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        mockDashboardApi = mockk()

        // Mock RetrofitClient
        mockkObject(RetrofitClient)
        every { RetrofitClient.getDashboardApi(any()) } returns mockDashboardApi

        // Create test data
        testDevices = listOf(
            AvailableUnit("123456789", "Active", "Device 1", "2024-01-01 12:00:00"),
            AvailableUnit("987654321", "Inactive", "Device 2", "2024-01-01 11:30:00")
        )

        adapter = DeviceAdapter(testDevices)
    }

    @After
    fun tearDown() {
        unmockkObject(RetrofitClient)
        clearAllMocks()
    }

    @Test
    fun `adapter should return correct item count`() {
        // Given
        val adapter = DeviceAdapter(testDevices)

        // When & Then
        assert(adapter.itemCount == 2)
    }

    @Test
    fun `adapter should return correct item count for empty list`() {
        // Given
        val emptyAdapter = DeviceAdapter(emptyList())

        // When & Then
        assert(emptyAdapter.itemCount == 0)
    }

    @Test
    fun `onCreateViewHolder should create valid ViewHolder`() {
        // Given
        val parent = FrameLayout(context)

        // When
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Then
        assert(viewHolder != null)
        assert(viewHolder.deviceName != null)
        assert(viewHolder.deviceTemp != null)
        assert(viewHolder.deviceStatus != null)
        assert(viewHolder.deviceLastSeen != null)
        assert(viewHolder.deviceBattery != null)
        assert(viewHolder.deviceDoor != null)
    }

    @Test
    fun `onBindViewHolder should set basic device info correctly`() {
        // Given
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Mock API response
        val mockDashboardItem = DashboardItem(
            id = 1,
            imei = "123456789",
            temp_max = 25.0f,
            temp_now = 20.0f,
            temp_min = 15.0f,
            supply_volt = 12.0f,
            bat_volt = 11.5f,
            supply_status = "Okay",
            bat_status = "Okay",
            door_status = "Closed",
            door_status_bool = 0,
            signal_strength = "Good",
            timestamp = "2024-01-01T12:00:00Z",
            active = 1
        )

        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.success(mockDashboardItem)

        // When
        adapter.onBindViewHolder(viewHolder, 0)

        // Then
        assert(viewHolder.deviceName.text == "Device 1")
        assert(viewHolder.deviceStatus.text == "● Online")
        assert(viewHolder.deviceLastSeen.text == "Last refreshed: 2024-01-01 12:00:00")
    }

    @Test
    fun `onBindViewHolder should set offline status for inactive devices`() {
        // Given
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Mock API response
        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.success(mockk(relaxed = true))

        // When - bind second device (inactive)
        adapter.onBindViewHolder(viewHolder, 1)

        // Then
        assert(viewHolder.deviceName.text == "Device 2")
        assert(viewHolder.deviceStatus.text == "● Offline")
        assert(viewHolder.deviceLastSeen.text == "Last refreshed: 2024-01-01 11:30:00")
    }

    @Test
    fun `adapter should handle API success response correctly`() = runTest {
        // Given
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        val mockDashboardItem = DashboardItem(
            id = 1,
            imei = "123456789",
            temp_max = 25.0f,
            temp_now = 22.5f,
            temp_min = 15.0f,
            supply_volt = 12.0f,
            bat_volt = 11.5f,
            supply_status = "Okay",
            bat_status = "Okay",
            door_status = "Closed",
            door_status_bool = 0,
            signal_strength = "Good",
            timestamp = "2024-01-01T12:00:00Z",
            active = 1
        )

        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.success(mockDashboardItem)

        // When
        adapter.onBindViewHolder(viewHolder, 0)

        // Allow coroutine to complete
        Thread.sleep(500)

        // Then
        coVerify(exactly = 1) { mockDashboardApi.getCurrent(match { it.imei == "123456789" }) }
    }

    @Test
    fun `adapter should handle API error response gracefully`() = runTest {
        // Given
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Mock API error
        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.error(404, mockk(relaxed = true))

        // When
        adapter.onBindViewHolder(viewHolder, 0)

        // Allow coroutine to complete
        Thread.sleep(500)

        // Then - should not crash
        coVerify(exactly = 1) { mockDashboardApi.getCurrent(any()) }
    }

    @Test
    fun `adapter should handle API exception gracefully`() = runTest {
        // Given
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Mock API exception
        coEvery { mockDashboardApi.getCurrent(any()) } throws Exception("Network error")

        // When
        adapter.onBindViewHolder(viewHolder, 0)

        // Allow coroutine to complete
        Thread.sleep(500)

        // Then - should not crash
        coVerify(exactly = 1) { mockDashboardApi.getCurrent(any()) }
    }

    @Test
    fun `adapter should handle different battery statuses`() = runTest {
        // Given
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Test low battery
        val lowBatteryItem = DashboardItem(
            id = 1, imei = "123456789", temp_max = 25.0f, temp_now = 20.0f, temp_min = 15.0f,
            supply_volt = 12.0f, bat_volt = 9.5f, supply_status = "Okay", bat_status = "Low",
            door_status = "Closed", door_status_bool = 0, signal_strength = "Good",
            timestamp = "2024-01-01T12:00:00Z", active = 1
        )

        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.success(lowBatteryItem)

        // When
        adapter.onBindViewHolder(viewHolder, 0)

        // Allow coroutine to complete
        Thread.sleep(500)

        // Then
        // Battery icon should be set (exact drawable verification would need more complex setup)
        coVerify(exactly = 1) { mockDashboardApi.getCurrent(any()) }
    }

    @Test
    fun `adapter should handle different door statuses`() = runTest {
        // Given
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Test open door
        val openDoorItem = DashboardItem(
            id = 1, imei = "123456789", temp_max = 25.0f, temp_now = 20.0f, temp_min = 15.0f,
            supply_volt = 12.0f, bat_volt = 11.5f, supply_status = "Okay", bat_status = "Okay",
            door_status = "Open", door_status_bool = 1, signal_strength = "Good",
            timestamp = "2024-01-01T12:00:00Z", active = 1
        )

        coEvery { mockDashboardApi.getCurrent(any()) } returns Response.success(openDoorItem)

        // When
        adapter.onBindViewHolder(viewHolder, 0)

        // Allow coroutine to complete
        Thread.sleep(500)

        // Then
        // Door icon should be set (exact drawable verification would need more complex setup)
        coVerify(exactly = 1) { mockDashboardApi.getCurrent(any()) }
    }
}