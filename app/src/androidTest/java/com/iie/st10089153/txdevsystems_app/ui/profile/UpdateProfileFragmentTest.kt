package com.iie.st10089153.txdevsystems_app.ui.profile

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.iie.st10089153.fragments.UpdateProfileFragment
import com.iie.st10089153.txdevsystems_app.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.After

@RunWith(AndroidJUnit4::class)
class UpdateProfileFragmentTest {

    private lateinit var scenario: FragmentScenario<UpdateProfileFragment>
    private val theme = com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar

    @Before fun setup() { }

    @After
    fun tearDown() { if (::scenario.isInitialized) scenario.close() }

    @Test
    fun testFragmentCreation() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        scenario.onFragment { fragment -> assert(fragment.isAdded) }
    }

    @Test
    fun testSampleDataPopulation() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etUsername)).check(matches(withText("Cherika.User")))
        onView(withId(R.id.etFirstName)).check(matches(withText("Cherika")))
        onView(withId(R.id.etLastName)).check(matches(withText("Bodde")))
        onView(withId(R.id.etCellNumber)).check(matches(withText("082 000 0000")))
        onView(withId(R.id.etEmail)).check(matches(withText("cherika.bodde@co.za")))
        onView(withId(R.id.etPhoneNumber)).check(matches(withText("011 000 0000")))
        onView(withId(R.id.etAddress)).check(matches(withText("Pam Straat 617")))
        onView(withId(R.id.tvAccountCreated)).check(matches(withText("2025-06-25 15:55:29")))
    }

    @Test
    fun testValidationWithEmptyUsername() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etUsername)).perform(clearText())
        onView(withId(R.id.btnSaveChanges)).perform(click())
        onView(withId(R.id.etUsername)).check(matches(hasErrorText("Username is required")))
    }

    @Test
    fun testValidationWithEmptyFirstName() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etFirstName)).perform(clearText())
        onView(withId(R.id.btnSaveChanges)).perform(click())
        onView(withId(R.id.etFirstName)).check(matches(hasErrorText("First name is required")))
    }

    @Test
    fun testValidationWithEmptyLastName() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etLastName)).perform(clearText())
        onView(withId(R.id.btnSaveChanges)).perform(click())
        onView(withId(R.id.etLastName)).check(matches(hasErrorText("Last name is required")))
    }

    @Test
    fun testValidationWithEmptyEmail() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etEmail)).perform(clearText())
        onView(withId(R.id.btnSaveChanges)).perform(click())
        onView(withId(R.id.etEmail)).check(matches(hasErrorText("Email is required")))
    }

    @Test
    fun testValidationWithInvalidEmail() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etEmail)).perform(clearText(), typeText("invalid-email"))
        onView(withId(R.id.btnSaveChanges)).perform(click())
        onView(withId(R.id.etEmail)).check(matches(hasErrorText("Please enter a valid email")))
    }

    @Test
    fun testValidationWithEmptyCellNumber() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etCellNumber)).perform(clearText())
        onView(withId(R.id.btnSaveChanges)).perform(click())
        onView(withId(R.id.etCellNumber)).check(matches(hasErrorText("Cell number is required")))
    }

    @Test
    fun testBackButtonFunctionality() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
    }

    @Test
    fun testUIElementsVisibility() {
        scenario = launchFragmentInContainer<UpdateProfileFragment>(themeResId = theme)
        onView(withId(R.id.etUsername)).check(matches(isDisplayed()))
        onView(withId(R.id.etFirstName)).check(matches(isDisplayed()))
        onView(withId(R.id.etLastName)).check(matches(isDisplayed()))
        onView(withId(R.id.etCellNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()))
        onView(withId(R.id.etPhoneNumber)).check(matches(isDisplayed()))
        onView(withId(R.id.etAddress)).check(matches(isDisplayed()))
        onView(withId(R.id.tvAccountCreated)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSaveChanges)).check(matches(isDisplayed()))
    }
}
