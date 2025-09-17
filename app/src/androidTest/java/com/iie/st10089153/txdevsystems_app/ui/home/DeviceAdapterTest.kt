package com.iie.st10089153.txdevsystems_app.ui.home

import android.content.Context
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceAdapterTest {

    private lateinit var context: Context
    private lateinit var adapter: DeviceAdapter
    private lateinit var testDevices: List<AvailableUnit>

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        testDevices = listOf(
            AvailableUnit("123456789", "Active", "Device 1", "2024-01-01 12:00:00"),
            AvailableUnit("987654321", "Inactive", "Device 2", "2024-01-01 11:30:00")
        )

        adapter = DeviceAdapter(testDevices)
    }

    @Test
    fun adapterReturnsCorrectItemCount() {
        assertEquals(2, adapter.itemCount)
        assertEquals(0, DeviceAdapter(emptyList()).itemCount)
    }

    @Test
    fun onCreateViewHolderCreatesValidViewHolder() {
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Verify that the ViewHolder was created successfully and has the required views
        viewHolder.apply {
            // These properties should exist and be accessible
            deviceName
            deviceTemp
            deviceStatus
            deviceLastSeen
            deviceBattery
            deviceDoor
        }
    }

    @Test
    fun onBindViewHolderSetsBasicDeviceInfo() {
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Bind the first device (Active)
        adapter.onBindViewHolder(viewHolder, 0)

        assertEquals("Device 1", viewHolder.deviceName.text)
        assertEquals("● Online", viewHolder.deviceStatus.text)
        assertEquals("Last refreshed: 2024-01-01 12:00:00", viewHolder.deviceLastSeen.text)
    }

    @Test
    fun onBindViewHolderSetsOfflineStatusForInactiveDevices() {
        val parent = FrameLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Bind the second device (Inactive)
        adapter.onBindViewHolder(viewHolder, 1)

        assertEquals("Device 2", viewHolder.deviceName.text)
        assertEquals("● Offline", viewHolder.deviceStatus.text)
        assertEquals("Last refreshed: 2024-01-01 11:30:00", viewHolder.deviceLastSeen.text)
    }

    @Test
    fun adapterHandlesDifferentDeviceStates() {
        val mixedDevices = listOf(
            AvailableUnit("111111111", "Active", "Online Device", "2024-01-01 14:00:00"),
            AvailableUnit("222222222", "Inactive", "Offline Device", "2024-01-01 13:00:00"),
            AvailableUnit("333333333", "Active", "Another Online", "2024-01-01 15:00:00")
        )

        val mixedAdapter = DeviceAdapter(mixedDevices)
        assertEquals(3, mixedAdapter.itemCount)

        val parent = FrameLayout(context)

        // Test active device
        val activeHolder = mixedAdapter.onCreateViewHolder(parent, 0)
        mixedAdapter.onBindViewHolder(activeHolder, 0)
        assertEquals("Online Device", activeHolder.deviceName.text)
        assertEquals("● Online", activeHolder.deviceStatus.text)

        // Test inactive device
        val inactiveHolder = mixedAdapter.onCreateViewHolder(parent, 0)
        mixedAdapter.onBindViewHolder(inactiveHolder, 1)
        assertEquals("Offline Device", inactiveHolder.deviceName.text)
        assertEquals("● Offline", inactiveHolder.deviceStatus.text)
    }

    @Test
    fun adapterHandlesEmptyDeviceList() {
        val emptyAdapter = DeviceAdapter(emptyList())
        assertEquals(0, emptyAdapter.itemCount)
    }

    @Test
    fun adapterHandlesDeviceWithEmptyName() {
        val devicesWithEmptyName = listOf(
            AvailableUnit("123456789", "Active", "", "2024-01-01 12:00:00"),
            AvailableUnit("987654321", "Active", "Valid Name", "2024-01-01 11:30:00")
        )

        val emptyNameAdapter = DeviceAdapter(devicesWithEmptyName)
        assertEquals(2, emptyNameAdapter.itemCount)

        val parent = FrameLayout(context)
        val viewHolder = emptyNameAdapter.onCreateViewHolder(parent, 0)

        // Test empty name device
        emptyNameAdapter.onBindViewHolder(viewHolder, 0)
        assertEquals("", viewHolder.deviceName.text)

        // Test normal name device
        emptyNameAdapter.onBindViewHolder(viewHolder, 1)
        assertEquals("Valid Name", viewHolder.deviceName.text)
    }
}