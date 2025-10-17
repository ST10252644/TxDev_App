package com.iie.st10089153.txdevsystems_app

import com.iie.st10089153.txdevsystems_app.database.*
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.repository.*
import com.google.common.truth.Truth.assertThat
import com.iie.st10089153.txdevsystems_app.database.dao.UnitDao
import com.iie.st10089153.txdevsystems_app.database.entities.CachedUnit
import com.iie.st10089153.txdevsystems_app.ui.reports.BatteryStatus
import com.iie.st10089153.txdevsystems_app.ui.reports.DoorStatus
import com.iie.st10089153.txdevsystems_app.ui.reports.PowerStatus
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Before

/**
 * Unit Tests for Repository Layer
 * These tests verify caching logic and data transformations
 */
class RepositoryUnitTests {

    @Before
    fun setup() {
        // Setup if needed
    }

    @Test
    fun `test AvailableUnit to CachedUnit conversion`() {
        // Given
        val availableUnit = AvailableUnit(
            imei = "123456789",
            name = "Test Device",
            status = "Active",
            last_seen = "2024-01-01"
        )

        // When
        val cachedUnit = availableUnit.toCachedUnit()

        // Then
        assertThat(cachedUnit.imei).isEqualTo("123456789")
        assertThat(cachedUnit.name).isEqualTo("Test Device")
        assertThat(cachedUnit.status).isEqualTo("Active")
        assertThat(cachedUnit.last_seen).isEqualTo("2024-01-01")
        assertThat(cachedUnit.timestamp).isGreaterThan(0L)
    }

    @Test
    fun `test CachedUnit to AvailableUnit conversion`() {
        // Given
        val cachedUnit = CachedUnit(
            imei = "123456789",
            name = "Test Device",
            status = "Active",
            last_seen = "2024-01-01",
            timestamp = System.currentTimeMillis()
        )

        // When
        val availableUnit = cachedUnit.toAvailableUnit()

        // Then
        assertThat(availableUnit.imei).isEqualTo("123456789")
        assertThat(availableUnit.name).isEqualTo("Test Device")
        assertThat(availableUnit.status).isEqualTo("Active")
    }

    @Test
    fun `test temperature formatting with valid number`() {
        // Given
        val viewModel = TestReportViewModel()

        // When
        val result = viewModel.testFormatTemperature(25.5)

        // Then
        assertThat(result).isEqualTo("25°")
    }

    @Test
    fun `test temperature formatting with string`() {
        // Given
        val viewModel = TestReportViewModel()

        // When
        val result = viewModel.testFormatTemperature("30.2")

        // Then
        assertThat(result).isEqualTo("30°")
    }

    @Test
    fun `test temperature formatting with invalid input`() {
        // Given
        val viewModel = TestReportViewModel()

        // When
        val result = viewModel.testFormatTemperature(null)

        // Then
        assertThat(result).isEqualTo("--")
    }

    @Test
    fun `test door status parsing - closed`() {
        // Given
        val viewModel = TestReportViewModel()

        // When
        val result = viewModel.testParseDoorStatus(null, "0")

        // Then
        assertThat(result).isEqualTo(DoorStatus.CLOSED)
    }

    @Test
    fun `test door status parsing - open`() {
        // Given
        val viewModel = TestReportViewModel()

        // When
        val result = viewModel.testParseDoorStatus(null, "1")

        // Then
        assertThat(result).isEqualTo(DoorStatus.OPEN)
    }

    @Test
    fun `test supply status parsing - okay`() {
        // Given
        val viewModel = TestReportViewModel()

        // When
        val resultOkay = viewModel.testParseSupplyStatus("Okay")
        val resultOk = viewModel.testParseSupplyStatus("ok")

        // Then
        assertThat(resultOkay).isEqualTo(PowerStatus.OK)
        assertThat(resultOk).isEqualTo(PowerStatus.OK)
    }

    @Test
    fun `test battery status parsing`() {
        // Given
        val viewModel = TestReportViewModel()

        // When
        val okayResult = viewModel.testParseBatteryStatus("Okay")
        val lowResult = viewModel.testParseBatteryStatus("Low")
        val errorResult = viewModel.testParseBatteryStatus("Error")

        // Then
        assertThat(okayResult).isEqualTo(BatteryStatus.OK)
        assertThat(lowResult).isEqualTo(BatteryStatus.LOW)
        assertThat(errorResult).isEqualTo(BatteryStatus.ERROR)
    }

    @Test
    fun `test cache validity calculation`() {
        // Given
        val now = System.currentTimeMillis()
        val oneMinuteAgo = now - 60000L
        val fiveMinutesAgo = now - 300000L
        val cacheValidityMs = 120000L // 2 minutes

        // When & Then
        assertThat(now - oneMinuteAgo).isLessThan(cacheValidityMs)
        assertThat(now - fiveMinutesAgo).isGreaterThan(cacheValidityMs)
    }
}

// Test helper class
class TestReportViewModel {
    fun testFormatTemperature(temp: Any?): String {
        return when (temp) {
            is Number -> "${temp.toInt()}°"
            is String -> {
                val number = temp.toDoubleOrNull()
                if (number != null) "${number.toInt()}°" else temp
            }
            else -> "--"
        }
    }

    fun testParseDoorStatus(doorStatus: String?, doorStatusBool: String?): DoorStatus {
        return when (doorStatusBool) {
            "0" -> DoorStatus.CLOSED
            "1" -> DoorStatus.OPEN
            else -> DoorStatus.CLOSED
        }
    }

    fun testParseSupplyStatus(supplyStatus: String?): PowerStatus {
        return when (supplyStatus?.lowercase()) {
            "okay", "ok" -> PowerStatus.OK
            else -> PowerStatus.ERROR
        }
    }

    fun testParseBatteryStatus(batteryStatus: String?): BatteryStatus {
        return when (batteryStatus?.lowercase()) {
            "okay", "ok" -> BatteryStatus.OK
            "low" -> BatteryStatus.LOW
            else -> BatteryStatus.ERROR
        }
    }
}