package com.iie.st10089153.txdevsystems_app.ui.reports

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportsRepositorySimpleTest {
    private lateinit var context: Context
    private lateinit var repository: ReportsRepository

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        repository = ReportsRepository(context)
    }

    @Test
    fun repositoryInitializesCorrectly() {
        assertNotNull(repository)
    }

    // Test enum values without network calls
    @Test
    fun doorStatusEnumValues() {
        assertEquals(2, DoorStatus.values().size)
        assertTrue(DoorStatus.values().contains(DoorStatus.OPEN))
        assertTrue(DoorStatus.values().contains(DoorStatus.CLOSED))
    }

    @Test
    fun powerStatusEnumValues() {
        assertEquals(2, PowerStatus.values().size)
        assertTrue(PowerStatus.values().contains(PowerStatus.OK))
        assertTrue(PowerStatus.values().contains(PowerStatus.ERROR))
    }

    @Test
    fun batteryStatusEnumValues() {
        assertEquals(3, BatteryStatus.values().size)
        assertTrue(BatteryStatus.values().contains(BatteryStatus.OK))
        assertTrue(BatteryStatus.values().contains(BatteryStatus.LOW))
        assertTrue(BatteryStatus.values().contains(BatteryStatus.ERROR))
    }
}