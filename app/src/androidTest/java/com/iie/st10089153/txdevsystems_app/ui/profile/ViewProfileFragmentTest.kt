package com.iie.st10089153.txdevsystems_app.ui.profile

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.iie.st10089153.fragments.ViewProfileFragment
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.network.AccountResponse
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient

import io.mockk.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import org.junit.After

@RunWith(AndroidJUnit4::class)
class ViewProfileFragmentTest {

    private lateinit var scenario: FragmentScenario<ViewProfileFragment>
    private val mockCall: Call<AccountResponse> = mockk()
    private val theme = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar

    @Before
    fun setup() {
        mockkObject(RetrofitClient)
        every { RetrofitClient.getProfileApi(any()) } returns mockk {
            every { getProfile() } returns mockCall
        }
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) scenario.close()
        unmockkAll()
    }

    @Test
    fun testFragmentCreation() {
        setupMockApiSuccess()
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)
        scenario.onFragment { fragment -> assert(fragment.isAdded) }
    }

    @Test
    fun testSuccessfulProfileFetch() {
        val mockAccountResponse = createMockAccountResponse(
            username = "test_user",
            firstName = "Test",
            lastName = "User",
            cell = "123456789",
            email = "test@example.com",
            officeNr = "987654321",
            address = "123 Test Street",
            timestamp = "2025-01-15 10:30:00"
        )
        setupMockApiSuccess(mockAccountResponse)
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)

        onView(withId(R.id.tvUsername)).check(matches(withText("test_user")))
        onView(withId(R.id.tvFirstName)).check(matches(withText("Test")))
        onView(withId(R.id.tvLastName)).check(matches(withText("User")))
        onView(withId(R.id.tvCellNumber)).check(matches(withText("123456789")))
        onView(withId(R.id.tvEmail)).check(matches(withText("test@example.com")))
        onView(withId(R.id.tvPhoneNumber)).check(matches(withText("987654321")))
        onView(withId(R.id.tvAddress)).check(matches(withText("123 Test Street")))
        onView(withId(R.id.tvAccountCreated)).check(matches(withText("2025-01-15 10:30:00")))
    }

    @Test
    fun testProfileFetchWithNullFields() {
        val mockAccountResponse = createMockAccountResponse(
            username = "test_user",
            firstName = "Test",
            lastName = "User",
            cell = null,
            email = null,
            officeNr = null,
            address = null,
            timestamp = null
        )
        setupMockApiSuccess(mockAccountResponse)
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)

        onView(withId(R.id.tvCellNumber)).check(matches(withText("N/A")))
        onView(withId(R.id.tvEmail)).check(matches(withText("N/A")))
        onView(withId(R.id.tvPhoneNumber)).check(matches(withText("N/A")))
        onView(withId(R.id.tvAddress)).check(matches(withText("N/A")))
        onView(withId(R.id.tvAccountCreated)).check(matches(withText("N/A")))
    }

    @Test
    fun testApiFailureResponse() {
        setupMockApiFailure(404, "Not Found")
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()))
        onView(withId(R.id.tvFirstName)).check(matches(isDisplayed()))
    }

    @Test
    fun testApiNetworkError() {
        setupMockApiNetworkError("Network error")
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()))
    }

    @Test
    fun testBackButtonFunctionality() {
        setupMockApiSuccess()
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)
        onView(withId(R.id.btnBack)).check(matches(isDisplayed())).check(matches(isClickable()))
    }

    @Test
    fun testEditButtonFunctionality() {
        setupMockApiSuccess()
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)
        onView(withId(R.id.btnEdit)).check(matches(isDisplayed())).check(matches(isClickable())).perform(click())
        scenario.onFragment { fragment -> assert(fragment.parentFragmentManager != null) }
    }

    @Test
    fun testUIElementsVisibility() {
        setupMockApiSuccess()
        scenario = launchFragmentInContainer<ViewProfileFragment>(themeResId = theme)
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()))
        onView(withId(R.id.tvFirstName)).check(matches(isDisplayed()))
        onView(withId(R.id.tvLastName)).check(matches(isDisplayed()))
        onView(withId(R.id.tvCellNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.tvEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.tvPhoneNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.tvAddress)).check(matches(isDisplayed()))
        onView(withId(R.id.tvAccountCreated)).check(matches(isDisplayed()))
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()))
        onView(withId(R.id.btnEdit)).check(matches(isDisplayed()))
    }

    // Helpers
    private fun createMockAccountResponse(
        username: String = "default_user",
        firstName: String = "Default",
        lastName: String = "User",
        cell: String? = "000000000",
        email: String? = "default@example.com",
        officeNr: String? = "111111111",
        address: String? = "Default Address",
        timestamp: String? = "2025-01-01 00:00:00"
    ): AccountResponse = mockk {
        every { this@mockk.username } returns username
        every { first_name } returns firstName
        every { last_name } returns lastName
        every { cell } returns cell
        every { email } returns email
        every { office_nr } returns officeNr
        every { address } returns address
        every { timestamp } returns timestamp
    }

    private fun setupMockApiSuccess(accountResponse: AccountResponse? = null) {
        val response = accountResponse ?: createMockAccountResponse()
        every { mockCall.enqueue(any()) } answers {
            val cb = arg<Callback<AccountResponse>>(0)
            cb.onResponse(mockCall, Response.success(response))
        }
    }

    private fun setupMockApiFailure(code: Int, message: String) {
        every { mockCall.enqueue(any()) } answers {
            val cb = arg<Callback<AccountResponse>>(0)
            cb.onResponse(mockCall, Response.error(code, message.toResponseBody()))
        }
    }

    private fun setupMockApiNetworkError(errorMessage: String) {
        every { mockCall.enqueue(any()) } answers {
            val cb = arg<Callback<AccountResponse>>(0)
            cb.onFailure(mockCall, RuntimeException(errorMessage))
        }
    }
}
