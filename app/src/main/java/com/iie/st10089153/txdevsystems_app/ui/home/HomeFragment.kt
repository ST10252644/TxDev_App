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
import android.widget.ImageView
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
import kotlin.random.Random

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

        // Bind Hello + Subtitle text
        homeViewModel.helloText.observe(viewLifecycleOwner) {
            binding.tvHello.text = it
        }

        homeViewModel.subtitleText.observe(viewLifecycleOwner) {
            binding.tvSubtitle.text = it
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

        // Call API for Active Units
        val api = RetrofitClient.getAvailableUnitsApi(requireContext())
        val call = api.getAvailableUnits(AvailableUnitsRequest(status = "Active"))

        call.enqueue(object : Callback<List<AvailableUnit>> {
            override fun onResponse(
                call: Call<List<AvailableUnit>>,
                response: Response<List<AvailableUnit>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    val units = response.body()!!

                    binding.homeContainer.removeAllViews()

                    for (unit in units) {
                        val cardView = layoutInflater.inflate(R.layout.device_card, binding.homeContainer, false)

                        val deviceName = cardView.findViewById<TextView>(R.id.deviceName)
                        val deviceTemp = cardView.findViewById<TextView>(R.id.deviceTemp)
                        val deviceStatus = cardView.findViewById<TextView>(R.id.deviceStatus)
                        val deviceLastSeen = cardView.findViewById<TextView>(R.id.deviceLastSeen)
                        val deviceBattery = cardView.findViewById<ImageView>(R.id.deviceBattery)

                       // Device name
                        deviceName.text = unit.name


                       // deviceTemp.text = "--" // Replace later when temp API available
                       // deviceStatus.text = if (unit.status.lowercase() == "online") "● Online" else "● Offline"
                       // deviceStatus.setTextColor(
                       //     if (unit.status.lowercase() == "online") 0xFFB8ED55.toInt() else 0xFFFF0000.toInt()
                       // )

                        // Dummy data
                        val tempValue = Random.nextInt(5, 31)
                        deviceTemp.text = tempValue.toString()

                        when {
                            tempValue < 15 -> {
                                // Cold = Red + Empty Battery
                                deviceTemp.setTextColor(0xFFFF0000.toInt())
                                deviceStatus.text = "● Offline"
                                deviceStatus.setTextColor(0xFFFF0000.toInt())
                                deviceBattery.setImageResource(R.drawable.ic_battery_empty)
                                deviceBattery.setColorFilter(0xFFFF0000.toInt())
                            }
                            tempValue == 15 -> {
                                // Neutral = Orange + Half Battery
                                val orange = 0xFFFFA500.toInt()
                                deviceTemp.setTextColor(orange)
                                deviceStatus.text = "● Standby"
                                deviceStatus.setTextColor(orange)
                                deviceBattery.setImageResource(R.drawable.ic_battery_half)
                                deviceBattery.setColorFilter(orange)
                            }
                            else -> {
                                // Warm = Green + Full Battery
                                val green = 0xFFB8ED55.toInt()
                                deviceTemp.setTextColor(green)
                                deviceStatus.text = "● Online"
                                deviceStatus.setTextColor(green)
                                deviceBattery.setImageResource(R.drawable.ic_battery_full)
                                deviceBattery.setColorFilter(green)
                            }
                        }

                        deviceLastSeen.text = "Last refreshed: ${unit.last_seen}"

                        binding.homeContainer.addView(cardView)
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