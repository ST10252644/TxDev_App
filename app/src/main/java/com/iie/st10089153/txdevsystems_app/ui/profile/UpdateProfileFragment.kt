package com.iie.st10089153.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.iie.st10089153.txdevsystems_app.databinding.FragmentEditProfileBinding
import com.iie.st10089153.txdevsystems_app.network.AccountResponse
import com.iie.st10089153.txdevsystems_app.network.Api.UpdateProfileRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        loadCurrentProfileData() // Load real data instead of sample data
    }

    private fun setupUI() {
        // Back button click listener
        binding.btnBack.setOnClickListener {
            navigateBackToViewProfile()
        }

        // Save button click listener
        binding.btnSaveChanges.setOnClickListener {
            if (validateFields()) {
                saveProfileChanges()
            }
        }

        // Disable editing for non-updatable fields
        disableNonEditableFields()
    }

    private fun disableNonEditableFields() {
        // Based on API docs, only address and first_name can be updated
        // Disable other fields
        binding.etUsername.isEnabled = false
        binding.etLastName.isEnabled = false
        binding.etCellNumber.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPhoneNumber.isEnabled = false

        // Make disabled fields visually distinct
        binding.etUsername.alpha = 0.6f
        binding.etLastName.alpha = 0.6f
        binding.etCellNumber.alpha = 0.6f
        binding.etEmail.alpha = 0.6f
        binding.etPhoneNumber.alpha = 0.6f

        // Show helper text
        Toast.makeText(requireContext(), "Only First Name and Address can be updated", Toast.LENGTH_LONG).show()
    }

    private fun loadCurrentProfileData() {
        val api = RetrofitClient.getProfileApi(requireContext())

        api.getProfile().enqueue(object : Callback<AccountResponse> {
            override fun onResponse(
                call: Call<AccountResponse>,
                response: Response<AccountResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val account = response.body()!!
                    populateFields(account)
                } else {
                    Log.e("UpdateProfile", "Failed to load profile: ${response.code()}")
                    Toast.makeText(requireContext(), "Failed to load profile data", Toast.LENGTH_SHORT).show()
                    // Fall back to sample data if API fails
                    populateWithSampleData()
                }
            }

            override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                Log.e("UpdateProfile", "Error loading profile: ${t.localizedMessage}")
                Toast.makeText(requireContext(), "Error loading profile data", Toast.LENGTH_SHORT).show()
                // Fall back to sample data if API fails
                populateWithSampleData()
            }
        })
    }

    private fun populateFields(account: AccountResponse) {
        binding.apply {
            etUsername.setText(account.username ?: "")
            etFirstName.setText(account.first_name ?: "")
            etLastName.setText(account.last_name ?: "")
            etCellNumber.setText(account.cell ?: "")
            etEmail.setText(account.email ?: "")
            etPhoneNumber.setText(account.office_nr ?: "")
            etAddress.setText(account.address ?: "")
            tvAccountCreated.text = account.timestamp ?: "N/A"
        }
    }

    private fun saveProfileChanges() {
        // Show loading state
        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Saving..."

        // Only send updatable fields
        val updateRequest = UpdateProfileRequest(
            address = binding.etAddress.text.toString().trim(),
            first_name = binding.etFirstName.text.toString().trim()
        )

        lifecycleScope.launch {
            try {
                val api = RetrofitClient.getProfileApi(requireContext())
                val response = withContext(Dispatchers.IO) {
                    api.updateProfile(updateRequest)
                }

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    Toast.makeText(requireContext(),
                        result.details ?: "Profile updated successfully!",
                        Toast.LENGTH_SHORT).show()

                    Log.d("UpdateProfile", "Profile updated: ${result.details}")

                    // Navigate back to view profile
                    navigateBackToViewProfile()
                } else {
                    Log.e("UpdateProfile", "Update failed: ${response.code()}")

                    if (response.code() == 422) {
                        Toast.makeText(requireContext(), "Validation error: Please check your input", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("UpdateProfile", "Exception during update: ${e.localizedMessage}")
                Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            } finally {
                // Reset button state
                binding.btnSaveChanges.isEnabled = true
                binding.btnSaveChanges.text = "Save Changes"
            }
        }
    }

    private fun navigateBackToViewProfile() {
        if (parentFragmentManager.backStackEntryCount > 0) {
            parentFragmentManager.popBackStack()
        } else {
            val viewProfileFragment = ViewProfileFragment()
            parentFragmentManager.beginTransaction()
                .replace(com.iie.st10089153.txdevsystems_app.R.id.profile_fragment_container, viewProfileFragment)
                .commit()
        }
    }

    private fun populateWithSampleData() {
        // Fallback sample data
        binding.apply {
            etUsername.setText("Cherika.User")
            etFirstName.setText("Cherika")
            etLastName.setText("Bodde")
            etCellNumber.setText("082 000 0000")
            etEmail.setText("cherika.bodde@co.za")
            etPhoneNumber.setText("011 000 0000")
            etAddress.setText("Pam Straat 617")
            tvAccountCreated.text = "2025-06-25 15:55:29"
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        // Only validate updatable fields
        if (binding.etFirstName.text.toString().trim().isEmpty()) {
            binding.etFirstName.error = "First name is required"
            isValid = false
        }

        if (binding.etAddress.text.toString().trim().isEmpty()) {
            binding.etAddress.error = "Address is required"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}