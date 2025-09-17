package com.iie.st10089153.txdevsystems_app.ui.home

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsRequest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleHomeFragmentTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun availableUnitDataClassCreatesCorrectly() {
        val unit = AvailableUnit(
            imei = "123456789",
            status = "Active",
            name = "Test Device",
            last_seen = "2024-01-01 12:00:00"
        )

        assertEquals("123456789", unit.imei)
        assertEquals("Active", unit.status)
        assertEquals("Test Device", unit.name)
        assertEquals("2024-01-01 12:00:00", unit.last_seen)
    }

    @Test
    fun availableUnitsRequestCreatesCorrectly() {
        val request = AvailableUnitsRequest(status = "Active")
        assertEquals("Active", request.status)

        val inactiveRequest = AvailableUnitsRequest(status = "Inactive")
        assertEquals("Inactive", inactiveRequest.status)
    }

    @Test
    fun multipleAvailableUnitsCanBeCreated() {
        val units = listOf(
            AvailableUnit("123456789", "Active", "Device 1", "2024-01-01 12:00:00"),
            AvailableUnit("987654321", "Active", "Device 2", "2024-01-01 11:30:00"),
            AvailableUnit("555666777", "Inactive", "Device 3", "2024-01-01 10:00:00")
        )

        assertEquals(3, units.size)
        assertEquals("Device 1", units[0].name)
        assertEquals("Active", units[0].status)
        assertEquals("Device 2", units[1].name)
        assertEquals("Active", units[1].status)
        assertEquals("Device 3", units[2].name)
        assertEquals("Inactive", units[2].status)
    }

    @Test
    fun emptyAvailableUnitListHandledCorrectly() {
        val emptyList = emptyList<AvailableUnit>()
        assertTrue(emptyList.isEmpty())
        assertEquals(0, emptyList.size)
    }

    @Test
    fun availableUnitWithEmptyFieldsHandledCorrectly() {
        val unitWithEmptyFields = AvailableUnit(
            imei = "",
            status = "",
            name = "",
            last_seen = ""
        )

        assertEquals("", unitWithEmptyFields.imei)
        assertEquals("", unitWithEmptyFields.status)
        assertEquals("", unitWithEmptyFields.name)
        assertEquals("", unitWithEmptyFields.last_seen)
    }

    @Test
    fun availableUnitsCanBeFilteredByStatus() {
        val allUnits = listOf(
            AvailableUnit("111111111", "Active", "Device A", "2024-01-01 12:00:00"),
            AvailableUnit("222222222", "Inactive", "Device B", "2024-01-01 11:30:00"),
            AvailableUnit("333333333", "Active", "Device C", "2024-01-01 10:00:00"),
            AvailableUnit("444444444", "Inactive", "Device D", "2024-01-01 09:00:00")
        )

        val activeUnits = allUnits.filter { it.status == "Active" }
        val inactiveUnits = allUnits.filter { it.status == "Inactive" }

        assertEquals(2, activeUnits.size)
        assertEquals(2, inactiveUnits.size)
        assertEquals("Device A", activeUnits[0].name)
        assertEquals("Device C", activeUnits[1].name)
        assertEquals("Device B", inactiveUnits[0].name)
        assertEquals("Device D", inactiveUnits[1].name)
    }

    @Test
    fun availableUnitsRequestSupportsOnlyActiveStatus() {
        val activeRequest = AvailableUnitsRequest("Active")
        assertEquals("Active", activeRequest.status)
    }

    @Test
    fun availableUnitsRequestSupportsOnlyInactiveStatus() {
        val inactiveRequest = AvailableUnitsRequest("Inactive")
        assertEquals("Inactive", inactiveRequest.status)
    }

    @Test
    fun contextIsAvailableInTest() {
        assertNotNull(context)
        assertNotNull(context.packageName)
        assertTrue(context.packageName.contains("txdevsystems"))
    }
}