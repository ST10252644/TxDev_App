package com.iie.st10089153.txdevsystems_app.ui.device

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
class DeviceSettingsFragmentTest {

    @Test
    fun fragment_displaysUI() {
        // Optional: pass minimal arguments if needed
        val bundle = Bundle().apply { putString("IMEI", "123456789012345") }

        // Launch the fragment in container
        launchFragmentInContainer<DeviceSettingsFragment>(
            fragmentArgs = bundle,
            themeResId = R.style.Theme_TxDevSystems_App
        )

        // Verify main UI elements are visible
        onView(withId(R.id.tvDeviceID)).check(matches(isDisplayed()))
        onView(withId(R.id.etDeviceName)).check(matches(isDisplayed()))
        onView(withId(R.id.etHighTemp)).check(matches(isDisplayed()))
        onView(withId(R.id.etLowTemp)).check(matches(isDisplayed()))
        onView(withId(R.id.etDoorAlertTime)).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerDoorType)).check(matches(isDisplayed()))
        onView(withId(R.id.btnSaveChanges)).check(matches(isDisplayed()))
    }
}
