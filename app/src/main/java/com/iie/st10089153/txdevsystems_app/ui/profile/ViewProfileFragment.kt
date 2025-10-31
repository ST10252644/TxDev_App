package com.iie.st10089153.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentViewProfileBinding
import com.iie.st10089153.txdevsystems_app.network.AccountResponse
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import com.iie.st10089153.txdevsystems_app.utils.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        // Fetch user profile data
        fetchUserProfile()

        // Handle logout button click
        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun logoutUser() {
        val sessionManager = SessionManager(requireContext())

        // Clear saved session using YOUR logout method
        sessionManager.logout()  // Changed from clearSession() to logout()

        // Navigate to LoginActivity and clear back stack
        val intent = Intent(requireContext(), com.iie.st10089153.txdevsystems_app.ui.login.LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun fetchUserProfile() {
        val api = RetrofitClient.getProfileApi(requireContext())

        api.getProfile().enqueue(object : Callback<AccountResponse> {
            override fun onResponse(
                call: Call<AccountResponse>,
                response: Response<AccountResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val account = response.body()!!

                    binding.apply {
                        // Update profile name at the top
                        tvProfileName.text = "${account.first_name} ${account.last_name}"

                        // Update all profile fields
                        tvUsername.text = account.username
                        tvFirstName.text = account.first_name
                        tvLastName.text = account.last_name
                        tvCellNumber.text = account.cell ?: "N/A"
                        tvEmail.text = account.email ?: "N/A"
                        tvPhoneNumber.text = account.office_nr ?: "N/A"
                        tvAddress.text = account.address ?: "N/A"
                        tvAccountCreated.text = account.timestamp ?: "N/A"
                    }
                } else {
                    Log.e("ProfileDebug", "API response failed: ${response.code()}")
                    // You could show an error message to the user here
                }
            }

            override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                Log.e("ProfileDebug", "API call failed: ${t.localizedMessage}")
                // You could show an error message to the user here
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}