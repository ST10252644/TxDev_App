package com.iie.st10089153.txdevsystems_app.ui.home


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.iie.st10089153.txdevsystems_app.ui.dashboard.DashboardViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class HomeViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var helloTextObserver: Observer<String>

    @Mock
    private lateinit var subtitleTextObserver: Observer<String>

    private lateinit var homeViewModel: HomeViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        homeViewModel = HomeViewModel()
    }

    @Test
    fun `helloText should return correct initial value`() {
        // Given
        homeViewModel.helloText.observeForever(helloTextObserver)

        // When
        val value = homeViewModel.helloText.value

        // Then
        assert(value == "Hello Person Nathan")
    }

    @Test
    fun `subtitleText should return correct initial value`() {
        // Given
        homeViewModel.subtitleText.observeForever(subtitleTextObserver)

        // When
        val value = homeViewModel.subtitleText.value

        // Then
        assert(value == "The following devices are active on your account.")
    }

    @Test
    fun `helloText LiveData should be observable`() {
        // Given
        homeViewModel.helloText.observeForever(helloTextObserver)

        // When
        val value = homeViewModel.helloText.value

        // Then
        assert(value != null)
        assert(homeViewModel.helloText.hasObservers())
    }

    @Test
    fun `subtitleText LiveData should be observable`() {
        // Given
        homeViewModel.subtitleText.observeForever(subtitleTextObserver)

        // When
        val value = homeViewModel.subtitleText.value

        // Then
        assert(value != null)
        assert(homeViewModel.subtitleText.hasObservers())
    }

    @Test
    fun `helloText should remain constant`() {
        // Given
        homeViewModel.helloText.observeForever(helloTextObserver)
        val initialValue = homeViewModel.helloText.value

        // When - simulate time passing
        Thread.sleep(100)
        val laterValue = homeViewModel.helloText.value

        // Then
        assert(initialValue == laterValue)
        assert(laterValue == "Hello Person Nathan")
    }

    @Test
    fun `subtitleText should remain constant`() {
        // Given
        homeViewModel.subtitleText.observeForever(subtitleTextObserver)
        val initialValue = homeViewModel.subtitleText.value

        // When - simulate time passing
        Thread.sleep(100)
        val laterValue = homeViewModel.subtitleText.value

        // Then
        assert(initialValue == laterValue)
        assert(laterValue == "The following devices are active on your account.")
    }
}

// Dashboard ViewModel Tests
@RunWith(RobolectricTestRunner::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var textObserver: Observer<String>

    private lateinit var dashboardViewModel: DashboardViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        dashboardViewModel = DashboardViewModel()
    }

    @Test
    fun `text should return correct initial value`() {
        // Given
        dashboardViewModel.text.observeForever(textObserver)

        // When
        val value = dashboardViewModel.text.value

        // Then
        assert(value == "This is dashboard Fragment")
    }

    @Test
    fun `text LiveData should be observable`() {
        // Given
        dashboardViewModel.text.observeForever(textObserver)

        // When
        val value = dashboardViewModel.text.value

        // Then
        assert(value != null)
        assert(dashboardViewModel.text.hasObservers())
    }



    @Test
    fun `ViewModel should handle multiple observers`() {
        // Given
        val observer1: Observer<String> = Observer { }
        val observer2: Observer<String> = Observer { }

        // When
        dashboardViewModel.text.observeForever(observer1)
        dashboardViewModel.text.observeForever(observer2)

        // Then
        assert(dashboardViewModel.text.hasObservers())

        // Cleanup
        dashboardViewModel.text.removeObserver(observer1)
        dashboardViewModel.text.removeObserver(observer2)
    }
}