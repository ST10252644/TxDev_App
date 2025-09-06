package com.iie.st10089153.txdevsystems_app.ui.reports

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.iie.st10089153.txdevsystems_app.databinding.FragmentReportsBinding
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val vm: ReportViewModel by viewModels()

    private lateinit var basicAdapter: ReportAdapter
    private lateinit var extendedAdapter: ReportExtendedAdapter
    private lateinit var triggersAdapter: TriggersAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        binding.recyclerViewReport.layoutManager = LinearLayoutManager(requireContext())

        basicAdapter = ReportAdapter(emptyList())
        extendedAdapter = ReportExtendedAdapter(emptyList())
        triggersAdapter = TriggersAdapter(emptyList())

        binding.recyclerViewReport.adapter = extendedAdapter

        vm.loading.observe(viewLifecycleOwner) { /* show/hide progress if you add one */ }

        vm.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        vm.noData.observe(viewLifecycleOwner) { noData ->
            if (noData == true) {
                Toast.makeText(requireContext(), "No data available for this IMEI and date range", Toast.LENGTH_LONG).show()
                // Clear adapters when no data is available
                basicAdapter = ReportAdapter(emptyList())
                extendedAdapter = ReportExtendedAdapter(emptyList())
                triggersAdapter = TriggersAdapter(emptyList())
                binding.recyclerViewReport.adapter = extendedAdapter
            }
        }

        vm.basicCurrent.observe(viewLifecycleOwner) { row ->
            row?.let {
                basicAdapter = ReportAdapter(listOf(it))
                binding.recyclerViewReport.adapter = basicAdapter
            }
        }

        vm.extendedRange.observe(viewLifecycleOwner) { rows ->
            if (rows.isNotEmpty()) {
                extendedAdapter.submit(rows)
                binding.recyclerViewReport.adapter = extendedAdapter
            } else {
                // This handles the case where we get 200 but empty list
                Toast.makeText(requireContext(), "No data available for selected range", Toast.LENGTH_LONG).show()
                extendedAdapter.submit(emptyList())
            }
        }

        vm.triggers.observe(viewLifecycleOwner) { rows ->
            if (rows.isNotEmpty()) {
                triggersAdapter.submit(rows)
                binding.recyclerViewReport.adapter = triggersAdapter
            } else {
                // This handles the case where we get 200 but empty list
                Toast.makeText(requireContext(), "No data available for selected range", Toast.LENGTH_LONG).show()
                triggersAdapter.submit(emptyList())
            }
        }

        // Observe specific error responses
        vm.apiError.observe(viewLifecycleOwner) { apiError ->
            apiError?.let { error ->
                when (error.code) {
                    404 -> {
                        Toast.makeText(requireContext(), "Unit not found or not authorized", Toast.LENGTH_LONG).show()
                        // Optionally navigate back
                        findNavController().popBackStack()
                    }
                    422 -> {
                        // Show validation error details in AlertDialog
                        showValidationErrorDialog(error.message ?: "Validation error occurred")
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Server error: ${error.code}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Read IMEI passed via navigation arguments
        val imeiArg = arguments?.getString("IMEI")
        if (imeiArg.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Missing IMEI for reports. Please open Reports from a device in Dashboard.", Toast.LENGTH_LONG).show()
            findNavController().popBackStack()
            return
        }

        // Default date range: last 24 hours
        val now = Date()
        val cal = Calendar.getInstance().apply { time = now }
        val stopIso = isoDateTime(now)

        cal.add(Calendar.HOUR_OF_DAY, -24)
        val startIso = isoDateTime(cal.time)

        // For triggers API wants yyyy-MM-dd
        val stopDay = day(now)
        val startDay = day(cal.time)

        vm.loadExtendedRange(imeiArg, startIso, stopIso)

        // Example hooks for buttons (if you add them to layout):
        // binding.buttonLoadCurrent.setOnClickListener { vm.loadBasicCurrent(imeiArg) }
        // binding.buttonLoadExtended.setOnClickListener { vm.loadExtendedRange(imeiArg, startIso, stopIso) }
        // binding.buttonLoadTriggers.setOnClickListener { vm.loadTriggers(imeiArg, startDay, stopDay) }
    }

    private fun showValidationErrorDialog(errorMessage: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Validation Error")
            .setMessage(errorMessage)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun isoDateTime(d: Date): String {
        val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return f.format(d)
    }

    private fun day(d: Date): String {
        val f = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return f.format(d)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}