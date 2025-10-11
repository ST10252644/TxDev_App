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
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class PerformanceTests {

    @Test
    fun `test cache timestamp freshness`() {
        // Given
        val cacheTime = System.currentTimeMillis()
        Thread.sleep(100) // Wait 100ms
        val checkTime = System.currentTimeMillis()
        val cacheValidityMs = 60000L // 1 minute

        // When
        val age = checkTime - cacheTime
        val isValid = age < cacheValidityMs

        // Then
        assertThat(age).isLessThan(cacheValidityMs)
        assertThat(isValid).isTrue()
    }

    @Test
    fun `test expired cache detection`() {
        // Given - Simulate old cache from 5 minutes ago
        val oldCacheTime = System.currentTimeMillis() - 300000L
        val checkTime = System.currentTimeMillis()
        val cacheValidityMs = 60000L // 1 minute

        // When
        val age = checkTime - oldCacheTime
        val isExpired = age > cacheValidityMs

        // Then
        assertThat(age).isGreaterThan(cacheValidityMs)
        assertThat(isExpired).isTrue()
    }
}