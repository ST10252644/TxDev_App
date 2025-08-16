package com.iie.st10089153.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iie.st10089153.txdevsystems_app.R

class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Create a simple container view for hosting profile fragments
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Automatically show the ViewProfileFragment when ProfileFragment loads
        if (savedInstanceState == null) {
            showViewProfileFragment()
        }
    }

    private fun showViewProfileFragment() {
        val viewProfileFragment = ViewProfileFragment()

        childFragmentManager.beginTransaction()
            .replace(R.id.profile_fragment_container, viewProfileFragment)
            .commit()
    }

    // Method to programmatically switch to edit profile
    fun showEditProfileFragment() {
        val editProfileFragment = UpdateProfileFragment()

        childFragmentManager.beginTransaction()
            .replace(R.id.profile_fragment_container, editProfileFragment)
            .addToBackStack("ViewProfile")
            .commit()
    }
}