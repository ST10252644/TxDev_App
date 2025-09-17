package com.iie.st10089153.txdevsystems_app.ui.reports

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportViewModelSimpleTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ReportViewModel
    private lateinit var application: Application

    @Before
    fun setup() {
        application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext as Application
        viewModel = ReportViewModel(application)
    }

    @Test
    fun viewModelInitializesCorrectly() {
        assertNotNull(viewModel)
        assertNotNull(viewModel.reportData)
        assertNotNull(viewModel.loading)
        assertNotNull(viewModel.error)
    }

    @Test
    fun initialStateIsCorrect() {
        // Check initial values
        assertEquals(false, viewModel.loading.value)
        assertEquals(null, viewModel.error.value)
        assertTrue(viewModel.reportData.value?.isEmpty() ?: true)
    }
}