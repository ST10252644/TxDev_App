package com.iie.st10089153.fragments

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
        setupClickListeners()
        fetchUserProfile()
    }

    override fun onResume() {
        super.onResume()
        // Refresh profile data when returning from edit mode
        fetchUserProfile()
    }

    private fun setupClickListeners() {
        binding.btnEdit.setOnClickListener {
            val editProfileFragment = UpdateProfileFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.profile_fragment_container, editProfileFragment)
                .addToBackStack("ViewProfile")
                .commit()
        }
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
                }
            }

            override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                Log.e("ProfileDebug", "API call failed: ${t.localizedMessage}")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}