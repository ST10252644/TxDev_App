package com.iie.st10089153.txdevsystems_app.ui.reports

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.iie.st10089153.txdevsystems_app.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportDataAdapterTest {
    private lateinit var adapter: ReportDataAdapter
    private lateinit var context: Context

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        adapter = ReportDataAdapter(emptyList())
    }

    @Test
    fun adapterInitializesWithZeroItems() {
        assertEquals(0, adapter.itemCount)
    }

    @Test
    fun updateDataChangesItemCount() {
        val testItems = listOf(
            ReportItem("20°", DoorStatus.CLOSED, PowerStatus.OK, BatteryStatus.OK, "2024-01-01 12:00:00"),
            ReportItem("25°", DoorStatus.OPEN, PowerStatus.ERROR, BatteryStatus.LOW, "2024-01-01 13:00:00")
        )
        adapter.updateData(testItems)
        assertEquals(2, adapter.itemCount)
    }

    // Simplified test without MockK - just test that ViewHolder is created
    @Test
    fun onCreateViewHolderReturnsValidViewHolder() {
        val layoutInflater = LayoutInflater.from(context)
        // Create a proper ViewGroup parent - LinearLayout works well for this
        val parent = android.widget.LinearLayout(context)

        val viewHolder = adapter.onCreateViewHolder(parent, 0)
        assertNotNull(viewHolder)
    }

    // Test basic data binding without mocking
    @Test
    fun onBindViewHolderWithValidData() {
        val testItem = ReportItem("20°", DoorStatus.CLOSED, PowerStatus.OK, BatteryStatus.OK, "2024-01-01 12:00:00")
        adapter.updateData(listOf(testItem))

        val parent = android.widget.LinearLayout(context)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // This should not throw an exception
        adapter.onBindViewHolder(viewHolder, 0)
        assertEquals(1, adapter.itemCount)
    }
}