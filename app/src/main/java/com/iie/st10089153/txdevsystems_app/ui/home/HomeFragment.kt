package com.iie.st10089153.txdevsystems_app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.iie.st10089153.txdevsystems_app.databinding.FragmentHomeBinding
import com.iie.st10089153.txdevsystems_app.network.Api.AccountResponse
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsRequest
import com.iie.st10089153.txdevsystems_app.network.Api.LookupAccountRequest
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        setupObservers()
        setupRecyclerView()
        setupSwipeRefresh()
        loadUserInfo()
        loadDevices()

        return binding.root
    }


    private fun setupObservers() {
        homeViewModel.greetingText.observe(viewLifecycleOwner) { greeting ->
            binding.greetingText.text = greeting
        }

        homeViewModel.subtitleText.observe(viewLifecycleOwner) { subtitle ->
            binding.subtitleText.text = subtitle
        }
    }

    private fun setupRecyclerView() {
        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshHome.setOnRefreshListener {
            loadUserInfo()
            loadDevices()
        }
    }

    private fun loadUserInfo() {
        val accountApi = RetrofitClient.getAccountApi(requireContext())
        val accountCall = accountApi.lookupAccount(LookupAccountRequest())

        accountCall.enqueue(object : Callback<AccountResponse> {
            override fun onResponse(
                call: Call<AccountResponse>,
                response: Response<AccountResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val account = response.body()!!
                    homeViewModel.updateGreeting(account.username)
                } else {
                    // Keep default greeting if account lookup fails
                    android.util.Log.w("HomeFragment", "Account lookup failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<AccountResponse>, t: Throwable) {
                android.util.Log.e("HomeFragment", "Account lookup error: ${t.localizedMessage}")
                // Keep default greeting if account lookup fails
            }
        })
    }

    private fun loadDevices() {
        val api = RetrofitClient.getAvailableUnitsApi(requireContext())
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "All"))

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(
                call: Call<List<AvailableUnit>>,
                response: Response<List<AvailableUnit>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val units = response.body()!!

                    if (units.isNotEmpty()) {
                        // Create sectioned list
                        val sectionedItems = SectionedDeviceAdapter.createSectionedList(units)
                        binding.deviceRecyclerView.adapter = SectionedDeviceAdapter(sectionedItems)
                    } else {
                        Toast.makeText(requireContext(), "No devices found", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to load devices", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<AvailableUnit>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}