package com.iie.st10089153.txdevsystems_app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.room.Room
import com.iie.st10089153.txdevsystems_app.database.*
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLaunchTests {

    @Test
    fun testAppLaunches() {
        // Given - App context
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // Then - Package name should match
        assertThat(appContext.packageName).contains("txdevsystems")
    }

    @Test
    fun testDatabaseCanBeCreated() {
        // Given
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        // When
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()

        // Then
        assertThat(db).isNotNull()
        assertThat(db.isOpen).isTrue()

        // Cleanup
        db.close()
    }
}