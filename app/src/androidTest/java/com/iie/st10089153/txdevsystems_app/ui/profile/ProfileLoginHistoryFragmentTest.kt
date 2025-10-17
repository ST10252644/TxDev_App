package com.iie.st10089153.txdevsystems_app.ui.profile

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileLoginHistoryFragmentTest {

    private lateinit var fragment: ProfileLoginHistoryFragment

    @Before
    fun setup() {
        fragment = ProfileLoginHistoryFragment()
    }

    @Test
    fun testFragmentCreation() {
        // Test basic fragment instantiation
        assert(fragment is ProfileLoginHistoryFragment)
    }


}