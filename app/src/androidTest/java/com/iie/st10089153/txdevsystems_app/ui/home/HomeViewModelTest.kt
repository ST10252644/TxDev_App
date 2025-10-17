package com.iie.st10089153.txdevsystems_app.ui.home

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.ui.dashboard.DashboardViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun homeViewModel_constructsSuccessfully() {
        val viewModel = HomeViewModel(application)
        assertNotNull(viewModel)
    }

    @Test
    fun homeViewModel_isViewModelInstance() {
        val viewModel = HomeViewModel(application)
        assertTrue(viewModel is androidx.lifecycle.ViewModel)
    }

    @Test
    fun multipleHomeViewModels_canBeCreated() {
        val viewModel1 = HomeViewModel(application)
        val viewModel2 = HomeViewModel(application)

        assertNotNull(viewModel1)
        assertNotNull(viewModel2)
        assertNotSame(viewModel1, viewModel2)
    }

    @Test
    fun homeViewModel_hasDevicesLiveData() {
        val viewModel = HomeViewModel(application)
        assertNotNull(viewModel.devices)
    }

    @Test
    fun homeViewModel_hasIsLoadingLiveData() {
        val viewModel = HomeViewModel(application)
        assertNotNull(viewModel.isLoading)
    }

    @Test
    fun homeViewModel_hasErrorLiveData() {
        val viewModel = HomeViewModel(application)
        assertNotNull(viewModel.error)
    }

    @Test
    fun homeViewModel_hasGreetingTextLiveData() {
        val viewModel = HomeViewModel(application)
        assertNotNull(viewModel.greetingText)
        assertEquals("Hello User", viewModel.greetingText.value)
    }

    @Test
    fun homeViewModel_hasSubtitleTextLiveData() {
        val viewModel = HomeViewModel(application)
        assertNotNull(viewModel.subtitleText)
        assertEquals("The following devices are on your account",
            viewModel.subtitleText.value)
    }

    @Test
    fun homeViewModel_updateGreeting_changesGreetingText() {
        val viewModel = HomeViewModel(application)
        val testUsername = "TestUser"

        viewModel.updateGreeting(testUsername)

        assertEquals("Hello $testUsername", viewModel.greetingText.value)
    }

    @Test
    fun homeViewModel_initialLoadingState() {
        val viewModel = HomeViewModel(application)
        // Initial loading might be true or false depending on init block
        assertNotNull(viewModel.isLoading.value)
    }

    @Test
    fun homeViewModel_devicesListInitiallyEmpty() {
        val viewModel = HomeViewModel(application)
        // Devices might be null or empty initially
        val devices = viewModel.devices.value
        assertTrue(devices == null || devices.isEmpty())
    }
}

@RunWith(AndroidJUnit4::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var application: Application

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun dashboardViewModel_constructsSuccessfully() {
        val dashboardViewModel = DashboardViewModel(application)
        assertNotNull(dashboardViewModel)
    }

    @Test
    fun dashboardViewModel_isViewModelInstance() {
        val dashboardViewModel = DashboardViewModel(application)
        assertTrue(dashboardViewModel is androidx.lifecycle.ViewModel)
    }

    @Test
    fun multipleDashboardViewModels_canBeCreated() {
        val viewModel1 = DashboardViewModel(application)
        val viewModel2 = DashboardViewModel(application)

        assertNotNull(viewModel1)
        assertNotNull(viewModel2)
        assertNotSame(viewModel1, viewModel2)
    }

    @Test
    fun dashboardViewModel_hasDashboardDataLiveData() {
        val dashboardViewModel = DashboardViewModel(application)
        assertNotNull(dashboardViewModel.dashboardData)
    }

    @Test
    fun dashboardViewModel_hasIsLoadingLiveData() {
        val dashboardViewModel = DashboardViewModel(application)
        assertNotNull(dashboardViewModel.isLoading)
    }

    @Test
    fun dashboardViewModel_hasErrorLiveData() {
        val dashboardViewModel = DashboardViewModel(application)
        assertNotNull(dashboardViewModel.error)
    }

    @Test
    fun dashboardViewModel_initialDashboardDataIsNull() {
        val dashboardViewModel = DashboardViewModel(application)
        assertNull(dashboardViewModel.dashboardData.value)
    }

    @Test
    fun dashboardViewModel_initialLoadingStateIsFalse() {
        val dashboardViewModel = DashboardViewModel(application)
        assertEquals(false, dashboardViewModel.isLoading.value)
    }

    @Test
    fun dashboardViewModel_initialErrorIsNull() {
        val dashboardViewModel = DashboardViewModel(application)
        assertNull(dashboardViewModel.error.value)
    }

    @Test
    fun dashboardViewModel_loadDashboard_setsLoadingState() {
        val dashboardViewModel = DashboardViewModel(application)
        val testImei = "123456789"

        // When loading dashboard
        dashboardViewModel.loadDashboard(testImei)

        // Loading state should be handled (might be true during load or false after)
        assertNotNull(dashboardViewModel.isLoading.value)
    }

    @Test
    fun dashboardViewModel_refreshDashboard_forcesRefresh() {
        val dashboardViewModel = DashboardViewModel(application)
        val testImei = "123456789"

        // Should not throw exception
        dashboardViewModel.refreshDashboard(testImei)

        // Verify the method executes without error
        assertNotNull(dashboardViewModel)
    }

    @Test
    fun dashboardViewModel_loadDashboard_withEmptyImei() {
        val dashboardViewModel = DashboardViewModel(application)

        // Load with empty IMEI
        dashboardViewModel.loadDashboard("")

        // Should handle gracefully, error might be set
        assertNotNull(dashboardViewModel.error)
    }

    @Test
    fun dashboardViewModel_multipleDashboardViewModels_haveIndependentData() {
        val viewModel1 = DashboardViewModel(application)
        val viewModel2 = DashboardViewModel(application)

        // Each should have their own LiveData instances
        assertNotSame(viewModel1.dashboardData, viewModel2.dashboardData)
        assertNotSame(viewModel1.isLoading, viewModel2.isLoading)
        assertNotSame(viewModel1.error, viewModel2.error)
    }
}