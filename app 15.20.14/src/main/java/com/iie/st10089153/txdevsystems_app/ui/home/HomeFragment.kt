package com.iie.st10089153.txdevsystems_app.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import android.widget.Button
import android.view.ViewGroup.LayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.iie.st10089153.txdevsystems_app.R
import androidx.navigation.fragment.findNavController
import com.iie.st10089153.txdevsystems_app.databinding.FragmentHomeBinding
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsRequest
import com.iie.st10089153.txdevsystems_app.ui.login.LoginActivity
import com.iie.st10089153.txdevsystems_app.network.RetrofitClient

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Observe ViewModel text
        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }


        //Log.d("HomeFragment", "buttonGoToDashboard = ${binding.buttonGoToDashboard}")

        // Set click listener on the dashboard button
//        binding.buttonGoToDashboard.setOnClickListener {
//            Toast.makeText(requireContext(), "Dashboard button pressed", Toast.LENGTH_SHORT).show()
//            Log.d("HomeFragment", "Dashboard button clicked")
//
//            try {
//                findNavController().navigate(R.id.action_home_to_dashboard)
//                Log.d("HomeFragment", "Navigation to Dashboard triggered successfully")
//            } catch (e: Exception) {
//                Log.e("HomeFragment", "Navigation failed: ${e.message}", e)
//            }
//        }

        // Call /available_units/ and create buttons dynamically
        val api = RetrofitClient.getAvailableUnitsApi(requireContext())
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "Active"))

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(
                call: Call<List<AvailableUnit>>,
                response: Response<List<AvailableUnit>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val units = response.body()!!

                    for (unit in units) {
                        val imeiButton = Button(requireContext()).apply {
                            text = "IMEI: ${unit.imei}"
                            layoutParams = LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT
                            )
                            setOnClickListener {
                                val bundle = Bundle().apply {
                                    putString("IMEI", unit.imei)
                                }
                                findNavController().navigate(R.id.action_home_to_dashboard, bundle)

                            }
                        }
                        binding.homeContainer.addView(imeiButton)
                    }
                } else {
                    Toast.makeText(requireContext(), "No units found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<AvailableUnit>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        })


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
