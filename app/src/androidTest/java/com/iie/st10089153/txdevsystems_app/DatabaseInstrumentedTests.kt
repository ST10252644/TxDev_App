package com.iie.st10089153.txdevsystems_app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.room.Room
import com.iie.st10089153.txdevsystems_app.database.*
import com.google.common.truth.Truth.assertThat
import com.iie.st10089153.txdevsystems_app.database.dao.ConfigDao
import com.iie.st10089153.txdevsystems_app.database.dao.DashboardDao
import com.iie.st10089153.txdevsystems_app.database.dao.UnitDao
import com.iie.st10089153.txdevsystems_app.database.entities.CachedConfig
import com.iie.st10089153.txdevsystems_app.database.entities.CachedDashboard
import com.iie.st10089153.txdevsystems_app.database.entities.CachedUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented tests for Room Database
 * These run on an Android device/emulator
 */
@RunWith(AndroidJUnit4::class)
class DatabaseInstrumentedTests {

    private lateinit var database: AppDatabase
    private lateinit var unitDao: UnitDao
    private lateinit var dashboardDao: DashboardDao
    private lateinit var configDao: ConfigDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        unitDao = database.unitDao()
        dashboardDao = database.dashboardDao()
        configDao = database.configDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun testInsertAndRetrieveUnit() = runBlocking {
        // Given
        val unit = CachedUnit(
            imei = "123456789",
            name = "Test Device",
            status = "Active",
            last_seen = "2024-01-01",
            timestamp = System.currentTimeMillis()
        )

        // When
        unitDao.insertUnits(listOf(unit))
        val retrieved = unitDao.getValidUnits(0)

        // Then
        assertThat(retrieved).hasSize(1)
        assertThat(retrieved[0].imei).isEqualTo("123456789")
        assertThat(retrieved[0].name).isEqualTo("Test Device")
    }

    @Test
    fun testInsertAndRetrieveDashboard() = runBlocking {
        // Given
        val dashboard = CachedDashboard(
            imei = "123456789",
            temp_now = "5.0",
            temp_min = "2.0",
            temp_max = "8.0",
            door_status = "Closed",
            door_status_bool = 0,
            supply_status = "Okay",
            bat_volt = "12.5",
            bat_status = "Okay",
            timestamp = System.currentTimeMillis()
        )

        // When
        dashboardDao.insertDashboard(dashboard)
        val retrieved = dashboardDao.getDashboardByImei("123456789")

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.imei).isEqualTo("123456789")
        assertThat(retrieved?.temp_now).isEqualTo("5.0")
        assertThat(retrieved?.door_status).isEqualTo("Closed")
    }

    @Test
    fun testInsertAndRetrieveConfig() = runBlocking {
        // Given
        val config = CachedConfig(
            imei = "123456789",
            unit_id = "Test Unit",
            temp_max = "8",
            temp_min = "2",
            door_alarm_hour = "0",
            door_alarm_min = "15",
            switch_polarity = "1",
            config_type = "0",
            data_resend_min = "20",
            network = "Vodacom",
            remaining_data = "250MB",
            timestamp = System.currentTimeMillis()
        )

        // When
        configDao.insertConfig(config)
        val retrieved = configDao.getConfigByImei("123456789")

        // Then
        assertThat(retrieved).isNotNull()
        assertThat(retrieved?.unit_id).isEqualTo("Test Unit")
        assertThat(retrieved?.temp_max).isEqualTo("8")
        assertThat(retrieved?.door_alarm_min).isEqualTo("15")
    }

    @Test
    fun testDeleteExpiredUnits() = runBlocking {
        // Given
        val oldTimestamp = System.currentTimeMillis() - 3600000L // 1 hour ago
        val recentTimestamp = System.currentTimeMillis()

        val oldUnit = CachedUnit("111", "Old Device", "Active", "2024-01-01", oldTimestamp)
        val recentUnit = CachedUnit("222", "Recent Device", "Active", "2024-01-02", recentTimestamp)

        // When
        unitDao.insertUnits(listOf(oldUnit, recentUnit))
        unitDao.deleteExpiredUnits(System.currentTimeMillis() - 1800000L) // Delete older than 30 min

        val remaining = unitDao.getValidUnits(0)

        // Then
        assertThat(remaining).hasSize(1)
        assertThat(remaining[0].imei).isEqualTo("222")
    }

    @Test
    fun testCacheReplacement() = runBlocking {
        // Given
        val dashboard1 = CachedDashboard(
            imei = "123456789",
            temp_now = "5.0",
            temp_min = "2.0",
            temp_max = "8.0",
            door_status = "Closed",
            door_status_bool = 0,
            supply_status = "Okay",
            bat_volt = "12.5",
            bat_status = "Okay",
            timestamp = System.currentTimeMillis()
        )

        // When - Insert first version
        dashboardDao.insertDashboard(dashboard1)

        // Insert updated version with same IMEI
        val dashboard2 = dashboard1.copy(temp_now = "6.0")
        dashboardDao.insertDashboard(dashboard2)

        val retrieved = dashboardDao.getDashboardByImei("123456789")

        // Then - Should have replaced, not duplicated
        assertThat(retrieved?.temp_now).isEqualTo("6.0")
    }

    @Test
    fun testAppContextIsValid() {
        // Given
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Then
        assertThat(appContext.packageName).isEqualTo("com.iie.st10089153.txdevsystems_app")
    }
}
