package com.iie.st10089153.txdevsystems_app.ui.home

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.txdevsystems_app.ui.dashboard.DashboardViewModel
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun homeViewModel_constructsSuccessfully() {
        val viewModel = HomeViewModel()
        assertNotNull(viewModel)
    }

    @Test
    fun homeViewModel_isViewModelInstance() {
        val viewModel = HomeViewModel()
        assertTrue(viewModel is androidx.lifecycle.ViewModel)
    }

    @Test
    fun multipleHomeViewModels_canBeCreated() {
        val viewModel1 = HomeViewModel()
        val viewModel2 = HomeViewModel()

        assertNotNull(viewModel1)
        assertNotNull(viewModel2)
        assertNotSame(viewModel1, viewModel2)
    }
}

@RunWith(AndroidJUnit4::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun dashboardViewModel_constructsSuccessfully() {
        val dashboardViewModel = DashboardViewModel()
        assertNotNull(dashboardViewModel)
    }

    @Test
    fun textLiveData_hasInitialValue() {
        val dashboardViewModel = DashboardViewModel()
        val initialValue = dashboardViewModel.text.value
        assertNotNull("Dashboard text should have an initial value", initialValue)
        assertFalse("Initial value should not be empty", initialValue!!.isEmpty())
    }

    @Test
    fun textLiveData_canBeObserved() {
        val dashboardViewModel = DashboardViewModel()

        var observedValue: String? = null
        val observer = Observer<String> { value ->
            observedValue = value
        }

        dashboardViewModel.text.observeForever(observer)

        // The observer should immediately receive the current value
        assertNotNull("Observer should receive initial value", observedValue)
        assertEquals("Observer should receive the same value as direct access",
            dashboardViewModel.text.value, observedValue)

        // Clean up
        dashboardViewModel.text.removeObserver(observer)
    }

    @Test
    fun textLiveData_supportsMultipleObservers() {
        val dashboardViewModel = DashboardViewModel()

        var observer1Value: String? = null
        var observer2Value: String? = null

        val observer1 = Observer<String> { value -> observer1Value = value }
        val observer2 = Observer<String> { value -> observer2Value = value }

        dashboardViewModel.text.observeForever(observer1)
        dashboardViewModel.text.observeForever(observer2)

        assertTrue("ViewModel should have observers", dashboardViewModel.text.hasObservers())

        // Both observers should receive the same initial value
        assertEquals("Both observers should receive same value", observer1Value, observer2Value)
        assertNotNull("Observer 1 should receive value", observer1Value)
        assertNotNull("Observer 2 should receive value", observer2Value)

        // Clean up
        dashboardViewModel.text.removeObserver(observer1)
        dashboardViewModel.text.removeObserver(observer2)

        assertFalse("ViewModel should have no observers after removal", dashboardViewModel.text.hasObservers())
    }

    @Test
    fun dashboardViewModel_isViewModelInstance() {
        val dashboardViewModel = DashboardViewModel()
        assertTrue(dashboardViewModel is androidx.lifecycle.ViewModel)
    }

    @Test
    fun multipleDashboardViewModels_canBeCreated() {
        val viewModel1 = DashboardViewModel()
        val viewModel2 = DashboardViewModel()

        assertNotNull(viewModel1)
        assertNotNull(viewModel2)
        assertNotSame(viewModel1, viewModel2)

        // Each should have their own text LiveData
        assertNotSame(viewModel1.text, viewModel2.text)
    }
}