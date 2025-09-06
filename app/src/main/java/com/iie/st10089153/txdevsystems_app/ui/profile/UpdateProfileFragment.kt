package com.iie.st10089153.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.iie.st10089153.txdevsystems_app.databinding.FragmentEditProfileBinding

class UpdateProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()

    }

    private fun setupUI() {
        // Back button click listener
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        // Save button click listener
        binding.btnSaveChanges.setOnClickListener {
            if (validateFields()) {
                Toast.makeText(requireContext(), "Profile saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun validateFields(): Boolean {
        var isValid = true

        if (binding.etUsername.text.toString().trim().isEmpty()) {
            binding.etUsername.error = "Username is required"
            isValid = false
        }

        if (binding.etFirstName.text.toString().trim().isEmpty()) {
            binding.etFirstName.error = "First name is required"
            isValid = false
        }

        if (binding.etLastName.text.toString().trim().isEmpty()) {
            binding.etLastName.error = "Last name is required"
            isValid = false
        }

        if (binding.etEmail.text.toString().trim().isEmpty()) {
            binding.etEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString()).matches()) {
            binding.etEmail.error = "Please enter a valid email"
            isValid = false
        }

        if (binding.etCellNumber.text.toString().trim().isEmpty()) {
            binding.etCellNumber.error = "Cell number is required"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}