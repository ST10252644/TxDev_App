package com.iie.st10089153.txdevsystems_app.ui.reports

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentReportsBinding
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val vm: ReportViewModel by viewModels()
    private lateinit var adapter: ReportDataAdapter
    private var currentImei: String = ""
    private var deviceName: String = "Device" // Default name, will be updated from navigation args

    // Date range variables
    private var startDate: Date = Date()
    private var endDate: Date = Date()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        super.onViewCreated(v, savedInstanceState)

        // Get IMEI and device name from navigation arguments
        currentImei = arguments?.getString("IMEI") ?: ""
        deviceName = arguments?.getString("name") ?: "Device"

        if (currentImei.isEmpty()) {
            Toast.makeText(requireContext(), "Missing IMEI for reports", Toast.LENGTH_LONG).show()
            return
        }

        setupUI()
        setupObservers()
        setupClickListeners()

        // Set device name immediately and load initial data
        binding.tvDeviceName.text = deviceName
        loadDayData()
    }

    private fun setupUI() {
        binding.recyclerViewReport.layoutManager = LinearLayoutManager(requireContext())
        adapter = ReportDataAdapter(emptyList())
        binding.recyclerViewReport.adapter = adapter

        // Set initial button states
        setActiveButton(binding.btnDay)
    }

    private fun setupObservers() {
        vm.reportData.observe(viewLifecycleOwner) { data ->
            adapter.updateData(data)
        }

        vm.isLoading.observe(viewLifecycleOwner) { isLoading ->  // ✅ Changed from loading
            // Show/hide refresh button loading state
            binding.btnRefresh.isEnabled = !isLoading
        }

        vm.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnDay.setOnClickListener {
            setActiveButton(binding.btnDay)
            loadDayData()
        }

        binding.btnWeek.setOnClickListener {
            setActiveButton(binding.btnWeek)
            loadWeekData()
        }

        binding.btnMonth.setOnClickListener {
            setActiveButton(binding.btnMonth)
            loadMonthData()
        }

        binding.btnCustomDate.setOnClickListener {
            showDateRangePicker()
        }

        binding.btnRefresh.setOnClickListener {
            refreshCurrentData()
        }
    }

    private fun setActiveButton(activeButton: android.widget.Button) {
        // Reset all buttons to inactive
        binding.btnDay.setBackgroundResource(R.drawable.filter_button_inactive)
        binding.btnWeek.setBackgroundResource(R.drawable.filter_button_inactive)
        binding.btnMonth.setBackgroundResource(R.drawable.filter_button_inactive)
        binding.btnCustomDate.setBackgroundResource(R.drawable.filter_button_inactive)

        binding.btnDay.setTextColor(resources.getColor(android.R.color.white, null))
        binding.btnWeek.setTextColor(resources.getColor(android.R.color.white, null))
        binding.btnMonth.setTextColor(resources.getColor(android.R.color.white, null))
        binding.btnCustomDate.setTextColor(resources.getColor(android.R.color.white, null))

        // Set active button
        activeButton.setBackgroundResource(R.drawable.filter_button_active)
        activeButton.setTextColor(resources.getColor(android.R.color.black, null))
    }

    private fun showDateRangePicker() {
        val calendar = Calendar.getInstance()

        // Start date picker
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val startCal = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                startDate = startCal.time

                // End date picker
                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDayOfMonth ->
                        val endCal = Calendar.getInstance().apply {
                            set(endYear, endMonth, endDayOfMonth, 23, 59, 59)
                            set(Calendar.MILLISECOND, 999)
                        }
                        endDate = endCal.time

                        if (endDate.before(startDate)) {
                            Toast.makeText(requireContext(), "End date must be after start date", Toast.LENGTH_SHORT).show()
                            return@DatePickerDialog
                        }

                        setActiveButton(binding.btnCustomDate)
                        loadCustomRangeData()
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    setTitle("Select End Date")
                }.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            setTitle("Select Start Date")
        }.show()
    }

    private fun refreshCurrentData() {
        // Check which button is currently active and reload that data
        when {
            isButtonActive(binding.btnDay) -> loadDayData()
            isButtonActive(binding.btnWeek) -> loadWeekData()
            isButtonActive(binding.btnMonth) -> loadMonthData()
            isButtonActive(binding.btnCustomDate) -> loadCustomRangeData()
            else -> loadDayData() // Default fallback
        }
    }

    private fun isButtonActive(button: android.widget.Button): Boolean {
        return button.currentTextColor == resources.getColor(android.R.color.black, null)
    }

    private fun loadDayData() {
        val now = Date()
        val cal = Calendar.getInstance().apply { time = now }

        // Today start (00:00:00)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startIso = isoDateTime(cal.time)

        // Today end (23:59:59)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val stopIso = isoDateTime(cal.time)

        vm.loadRangeData(currentImei, startIso, stopIso)
    }

    private fun loadWeekData() {
        val now = Date()
        val cal = Calendar.getInstance().apply { time = now }

        // 7 days ago
        cal.add(Calendar.DAY_OF_YEAR, -7)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startIso = isoDateTime(cal.time)

        // Now
        val stopIso = isoDateTime(now)

        vm.loadRangeData(currentImei, startIso, stopIso)
    }

    private fun loadMonthData() {
        val now = Date()
        val cal = Calendar.getInstance().apply { time = now }

        // 30 days ago
        cal.add(Calendar.DAY_OF_YEAR, -30)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val startIso = isoDateTime(cal.time)

        // Now
        val stopIso = isoDateTime(now)

        vm.loadRangeData(currentImei, startIso, stopIso)
    }

    private fun loadCustomRangeData() {
        val startIso = isoDateTime(startDate)
        val stopIso = isoDateTime(endDate)
        vm.loadRangeData(currentImei, startIso, stopIso)
    }

    private fun isoDateTime(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return formatter.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}