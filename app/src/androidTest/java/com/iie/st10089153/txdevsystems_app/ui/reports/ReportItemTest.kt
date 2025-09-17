package com.iie.st10089153.txdevsystems_app.ui.reports

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportItemTest {

    @Test
    fun reportItemCreatesCorrectly() {
        val item = ReportItem(
            tempNow = "20°",
            doorStatus = DoorStatus.CLOSED,
            powerStatus = PowerStatus.OK,
            batteryStatus = BatteryStatus.OK,
            timestamp = "2024-01-01 12:00:00"
        )

        assertEquals("20°", item.tempNow)
        assertEquals(DoorStatus.CLOSED, item.doorStatus)
        assertEquals(PowerStatus.OK, item.powerStatus)
        assertEquals(BatteryStatus.OK, item.batteryStatus)
        assertEquals("2024-01-01 12:00:00", item.timestamp)
    }

    @Test
    fun doorStatusEnumHasCorrectValues() {
        val values = DoorStatus.values()
        assertEquals(2, values.size)
        assertTrue(values.contains(DoorStatus.OPEN))
        assertTrue(values.contains(DoorStatus.CLOSED))
    }

    @Test
    fun powerStatusEnumHasCorrectValues() {
        val values = PowerStatus.values()
        assertEquals(2, values.size)
        assertTrue(values.contains(PowerStatus.OK))
        assertTrue(values.contains(PowerStatus.ERROR))
    }

    @Test
    fun batteryStatusEnumHasCorrectValues() {
        val values = BatteryStatus.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(BatteryStatus.OK))
        assertTrue(values.contains(BatteryStatus.LOW))
        assertTrue(values.contains(BatteryStatus.ERROR))
    }
}
