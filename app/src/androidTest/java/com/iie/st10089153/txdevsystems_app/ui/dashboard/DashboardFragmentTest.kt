package com.iie.st10089153.txdevsystems_app.ui.dashboard

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.txdevsystems_app.network.Api.CurrentRequest
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.DashboardItem
import com.iie.st10089153.txdevsystems_app.ui.dashboard.models.GaugeCard
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleDashboardFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun currentRequest_createdWithCorrectImei() {
        val testImei = "123456789"
        val request = CurrentRequest(testImei)
        assertEquals(testImei, request.imei)
    }

    @Test
    fun currentRequest_handlesEmptyImei() {
        val request = CurrentRequest("")
        assertEquals("", request.imei)
    }

    @Test
    fun currentRequest_handlesLongImei() {
        val longImei = "123456789012345678901234567890"
        val request = CurrentRequest(longImei)
        assertEquals(longImei, request.imei)
    }

    @Test
    fun dashboardItem_createdCorrectly() {
        val item = createMockDashboardItem("123456789")

        assertEquals(1, item.id)
        assertEquals("123456789", item.imei)
        assertEquals(30.0f, item.temp_max)
        assertEquals(22.5f, item.temp_now)
        assertEquals(10.0f, item.temp_min)
        assertEquals(12.0f, item.supply_volt)
        assertEquals(11.5f, item.bat_volt)
        assertEquals("Okay", item.supply_status)
        assertEquals("Okay", item.bat_status)
        assertEquals("Closed", item.door_status)
        assertEquals(0, item.door_status_bool)
        assertEquals("Good", item.signal_strength)
        assertEquals(1, item.active)
    }

    @Test
    fun gaugeCard_createdWithPowerStatus() {
        val powerGauge = GaugeCard(
            iconRes = android.R.drawable.ic_lock_power_off,
            statusText = "ON",
            name = "Power",
            gaugeImageRes = android.R.drawable.ic_lock_power_off
        )

        assertEquals("Power", powerGauge.name)
        assertEquals("ON", powerGauge.statusText)
        assertNull(powerGauge.measurement)
        assertNull(powerGauge.minValue)
        assertNull(powerGauge.maxValue)
        assertEquals(android.R.drawable.ic_lock_power_off, powerGauge.iconRes)
        assertEquals(android.R.drawable.ic_lock_power_off, powerGauge.gaugeImageRes)
    }

    @Test
    fun gaugeCard_createdWithBatteryStatusAndMeasurements() {
        val batteryGauge = GaugeCard(
            iconRes = android.R.drawable.ic_lock_power_off,
            statusText = "11.5",
            name = "Battery",
            measurement = "Volts",
            minValue = 0f,
            maxValue = 15f,
            gaugeImageRes = android.R.drawable.ic_lock_power_off
        )

        assertEquals("Battery", batteryGauge.name)
        assertEquals("11.5", batteryGauge.statusText)
        assertEquals("Volts", batteryGauge.measurement)
        assertEquals(0f, batteryGauge.minValue)
        assertEquals(15f, batteryGauge.maxValue)
    }

    @Test
    fun gaugeCard_createdWithTemperatureStatusAndRange() {
        val tempGauge = GaugeCard(
            iconRes = android.R.drawable.ic_lock_power_off,
            statusText = "22.5",
            name = "Temperature",
            measurement = "°C",
            minValue = 10f,
            maxValue = 30f,
            gaugeImageRes = android.R.drawable.ic_lock_power_off
        )

        assertEquals("Temperature", tempGauge.name)
        assertEquals("22.5", tempGauge.statusText)
        assertEquals("°C", tempGauge.measurement)
        assertEquals(10f, tempGauge.minValue)
        assertEquals(30f, tempGauge.maxValue)
    }

    @Test
    fun createsGaugeCardsFromDashboardItem() {
        val dashboardItem = createMockDashboardItem("123456789")
        val gaugeList = createGaugeCardsFromDashboardItem(dashboardItem)

        assertEquals(4, gaugeList.size) // Power, Battery, Temperature, Door

        val powerGauge = gaugeList.find { it.name == "Power" }
        assertNotNull(powerGauge)
        assertEquals("ON", powerGauge!!.statusText)

        val batteryGauge = gaugeList.find { it.name == "Battery" }
        assertNotNull(batteryGauge)
        assertEquals("11.5", batteryGauge!!.statusText)
        assertEquals("Volts", batteryGauge.measurement)

        val tempGauge = gaugeList.find { it.name == "Temperature" }
        assertNotNull(tempGauge)
        assertEquals("22.5", tempGauge!!.statusText)
        assertEquals("°C", tempGauge.measurement)

        val doorGauge = gaugeList.find { it.name == "Door" }
        assertNotNull(doorGauge)
        assertEquals("Closed", doorGauge!!.statusText)
    }

    @Test
    fun handlesDifferentPowerStatuses() {
        val dashboardItemOff = createMockDashboardItem("123456789", supplyStatus = "Error")
        val gaugeList = createGaugeCardsFromDashboardItem(dashboardItemOff)
        val powerGauge = gaugeList.find { it.name == "Power" }
        assertEquals("OFF", powerGauge?.statusText)
    }

    @Test
    fun handlesDifferentDoorStatuses() {
        val item = createMockDashboardItem("123456789", doorStatusBool = 1, doorStatus = "Open")
        val gaugeList = createGaugeCardsFromDashboardItem(item)
        val doorGauge = gaugeList.find { it.name == "Door" }
        assertEquals("Open", doorGauge?.statusText)
    }

    @Test
    fun handlesLowBatteryVoltage() {
        val lowBatteryItem = createMockDashboardItem("123456789", batVolt = 9.5f)
        val gaugeList = createGaugeCardsFromDashboardItem(lowBatteryItem)
        val batteryGauge = gaugeList.find { it.name == "Battery" }
        assertEquals("9.5", batteryGauge?.statusText)
    }

    @Test
    fun handlesHighTemperature() {
        val highTempItem = createMockDashboardItem("123456789", tempNow = 35.0f, tempMax = 40.0f)
        val gaugeList = createGaugeCardsFromDashboardItem(highTempItem)
        val tempGauge = gaugeList.find { it.name == "Temperature" }
        assertEquals("35.0", tempGauge?.statusText)
        assertEquals(40.0f, tempGauge?.maxValue)
    }

    @Test
    fun handlesInactiveDevice() {
        val inactiveItem = createMockDashboardItem("123456789", active = 0)
        assertEquals(0, inactiveItem.active)
    }

    @Test
    fun contextIsAvailable() {
        assertNotNull(context)
        assertTrue(context.packageName.contains("txdevsystems"))
    }

    // ----------------- Helper Methods -----------------

    private fun createMockDashboardItem(
        imei: String,
        supplyStatus: String = "Okay",
        doorStatusBool: Int = 0,
        doorStatus: String = "Closed",
        batVolt: Float = 11.5f,
        tempNow: Float = 22.5f,
        tempMax: Float = 30.0f,
        active: Int = 1
    ): DashboardItem {
        return DashboardItem(
            id = 1,
            imei = imei,
            temp_max = tempMax,
            temp_now = tempNow,
            temp_min = 10.0f,
            supply_volt = 12.0f,
            bat_volt = batVolt,
            supply_status = supplyStatus,
            bat_status = "Okay",
            door_status = doorStatus,
            door_status_bool = doorStatusBool,
            signal_strength = "Good",
            timestamp = "2024-01-01T12:00:00Z",
            active = active
        )
    }

    private fun createGaugeCardsFromDashboardItem(item: DashboardItem): List<GaugeCard> {
        // Simulate the fragment's gauge creation logic
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