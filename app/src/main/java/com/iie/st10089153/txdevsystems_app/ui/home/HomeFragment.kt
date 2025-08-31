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
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnit
import com.iie.st10089153.txdevsystems_app.network.Api.AvailableUnitsRequest
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

        // Setup RecyclerView
        binding.deviceRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Call API
        val api = RetrofitClient.getAvailableUnitsApi(requireContext())
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "All"))

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(
                call: Call<List<AvailableUnit>>,
                response: Response<List<AvailableUnit>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val units = response.body()!!
                    binding.deviceRecyclerView.adapter = DeviceAdapter(units)
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
