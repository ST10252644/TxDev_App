package com.iie.st10089153.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentViewProfileBinding

class ViewProfileFragment : Fragment() {

    private var _binding: FragmentViewProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()

    }

    private fun setupClickListeners() {
        // Back button - navigate back or close fragment
        binding.btnBack.setOnClickListener {
            // Option 1: Use back stack if available
            if (parentFragmentManager.backStackEntryCount > 0) {
                parentFragmentManager.popBackStack()
            } else {
                // Option 2: Close activity or handle as needed
                requireActivity().onBackPressed()
            }
        }

        // Edit button - navigate to edit profile
        binding.btnEdit.setOnClickListener {
            navigateToEditProfile()
        }
    }

    private fun navigateToEditProfile() {
        val editProfileFragment = UpdateProfileFragment()

        parentFragmentManager.beginTransaction()
            .replace(R.id.profile_fragment_container, editProfileFragment)
            .addToBackStack("ViewProfile")
            .commit()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}