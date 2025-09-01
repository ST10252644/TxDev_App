package com.iie.st10089153.txdevsystems_app.ui.chart

import android.os.Bundle
import androidx.fragment.app.testing.launchFragment  // or launchFragmentInContainer
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
class BatteryChartFragmentTest {

    @Test
    fun fragment_displaysUI() {
        // Pass minimal arguments if your fragment requires them
        val bundle = Bundle().apply { putString("IMEI", "123456789012345") }

        // Launch the fragment directly in container with your app theme
        launchFragmentInContainer<BatteryChartFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_TxDevSystems_App_Splash
        )

        // Check that all UI elements are visible
        onView(withId(R.id.tvScreenTitle)).check(matches(isDisplayed()))
        onView(withId(R.id.lineChartBattery)).check(matches(isDisplayed()))
        onView(withId(R.id.btnDay)).check(matches(isDisplayed()))
        onView(withId(R.id.btnWeek)).check(matches(isDisplayed()))
        onView(withId(R.id.btnMonth)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSelectDateBattery)).check(matches(isDisplayed()))
        onView(withId(R.id.tvRangeLabel)).check(matches(isDisplayed()))
    }
}
