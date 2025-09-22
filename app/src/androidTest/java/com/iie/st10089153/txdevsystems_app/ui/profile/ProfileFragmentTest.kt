package com.iie.st10089153.txdevsystems_app.ui.profile

import android.os.Bundle
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.fragments.ProfileFragment
import com.iie.st10089153.fragments.ViewProfileFragment
import com.iie.st10089153.fragments.UpdateProfileFragment
import com.iie.st10089153.txdevsystems_app.R
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.After

@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest {

    private lateinit var scenario: FragmentScenario<ProfileFragment>

    @Before
    fun setup() {
        // Setup test environment
    }

    @After
    fun tearDown() {
        if (::scenario.isInitialized) {
            scenario.close()
        }
    }

    @Test
    fun testFragmentCreation() {
        scenario = launchFragmentInContainer<ProfileFragment>()

        scenario.onFragment { fragment ->
            assert(fragment.isAdded)
        }
    }

    @Test
    fun testOnCreateViewInflatesCorrectLayout() {
        scenario = launchFragmentInContainer<ProfileFragment>()

        // Verify the fragment container is present
        onView(withId(R.id.profile_fragment_container))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testShowViewProfileFragmentOnCreate() {
        scenario = launchFragmentInContainer<ProfileFragment>(Bundle())

        scenario.onFragment { fragment ->
            // Verify that ViewProfileFragment is loaded by default
            val childFragments = fragment.childFragmentManager.fragments
            assert(childFragments.isNotEmpty())
            assert(childFragments[0] is ViewProfileFragment)
        }
    }

    @Test
    fun testShowEditProfileFragment() {
        scenario = launchFragmentInContainer<ProfileFragment>()

        scenario.onFragment { fragment ->
            // Call the method to show edit profile
            fragment.showEditProfileFragment()

            // Verify UpdateProfileFragment is now shown
            val childFragments = fragment.childFragmentManager.fragments
            assert(childFragments.any { it is UpdateProfileFragment })
        }
    }

    @Test
    fun testBackStackAfterShowingEditProfile() {
        scenario = launchFragmentInContainer<ProfileFragment>()

        scenario.onFragment { fragment ->
            val initialBackStackCount = fragment.childFragmentManager.backStackEntryCount

            // Show edit profile fragment
            fragment.showEditProfileFragment()

            // Verify back stack entry was added
            val newBackStackCount = fragment.childFragmentManager.backStackEntryCount
            assert(newBackStackCount == initialBackStackCount + 1)

            // Verify back stack entry name
            val backStackEntry = fragment.childFragmentManager.getBackStackEntryAt(newBackStackCount - 1)
            assert(backStackEntry.name == "ViewProfile")
        }
    }

    @Test
    fun testFragmentRecreationWithSavedInstanceState() {
        scenario = launchFragmentInContainer<ProfileFragment>()

        // Recreate the fragment to simulate configuration change
        scenario.recreate()

        scenario.onFragment { fragment ->
            // Verify fragment still works after recreation
            assert(fragment.isAdded)
            val childFragments = fragment.childFragmentManager.fragments
            assert(childFragments.isNotEmpty())
        }
    }
}