package com.iie.st10089153.txdevsystems_app.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentHomeBinding
import com.iie.st10089153.txdevsystems_app.ui.login.LoginActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var tvHello: TextView
    private lateinit var cardDevice1: LinearLayout
    private lateinit var cardDevice2: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvHello = view.findViewById(R.id.tvHello)
        cardDevice1 = view.findViewById(R.id.cardDevice1)
        cardDevice2 = view.findViewById(R.id.cardDevice2)

        fetchAvailableUnits()

        return view
    }

    private fun fetchAvailableUnits() {
        ApiClient.instance.getAvailableUnits("Active")
            .enqueue(object : Callback<List<UnitResponse>> {
                override fun onResponse(
                    call: Call<List<UnitResponse>>,
                    response: Response<List<UnitResponse>>
                ) {
                    if (response.isSuccessful) {
                        val devices = response.body() ?: emptyList()
                        updateUI(devices)
                    } else {
                        Toast.makeText(requireContext(), "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<UnitResponse>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Failed: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun updateUI(devices: List<UnitResponse>) {
        if (devices.isNotEmpty()) {
            bindDeviceData(cardDevice1, devices[0])
        }
        if (devices.size > 1) {
            bindDeviceData(cardDevice2, devices[1])
        }
    }

    private fun bindDeviceData(card: LinearLayout, device: UnitResponse) {
        val nameView = card.findViewById<TextView>(R.id.deviceName) // You’d add IDs in XML
        val tempView = card.findViewById<TextView>(R.id.deviceTemp)
        val statusView = card.findViewById<TextView>(R.id.deviceStatus)
        val batteryIcon = card.findViewById<ImageView>(R.id.deviceBattery)
        val doorIcon = card.findViewById<ImageView>(R.id.deviceDoor)
        val lastSeenView = card.findViewById<TextView>(R.id.deviceLastSeen)

        nameView.text = device.name
        tempView.text = "--" // API might give you this from another call
        statusView.text = if (device.status.lowercase() == "active") "● Online" else "● Offline"
        statusView.setTextColor(
            if (device.status.lowercase() == "active") 0xFFB8ED55.toInt() else 0xFFFF0000.toInt()
        )
        batteryIcon.setColorFilter(
            if (device.status.lowercase() == "active") 0xFFB8ED55.toInt() else 0xFFFF0000.toInt()
        )
        doorIcon.setColorFilter(
            if (device.status.lowercase() == "active") 0xFFFFFFFF.toInt() else 0xFFFF0000.toInt()
        )
        lastSeenView.text = "Last refreshed: ${device.last_seen}"
    }
}
