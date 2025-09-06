package com.iie.st10089153.txdevsystems_app.ui.chart

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.iie.st10089153.txdevsystems_app.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TempChartFragmentTest {

    @Test
    fun fragment_displaysUI() {
        // Provide minimal arguments if the fragment expects any
        val bundle = Bundle().apply { putString("IMEI", "123456789012345") }

        // Launch the fragment in a container with the app theme
        launchFragmentInContainer<TempChartFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_TxDevSystems_App
        )

        // Check that all main UI elements are visible
        onView(withId(R.id.tvScreenTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.lineChartTemp)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSelectDateTemp)).check(matches(isDisplayed()))
        onView(withId(R.id.tvRangeLabel)).check(matches(isDisplayed()))
    }
}
