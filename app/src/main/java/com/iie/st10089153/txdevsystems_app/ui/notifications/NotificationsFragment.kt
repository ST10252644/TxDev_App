package com.iie.st10089153.txdevsystems_app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsViewModel: NotificationsViewModel
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel = ViewModelProvider(this)[NotificationsViewModel::class.java]
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        setupRecyclerView()
        setupFilterListener()
        setupSwipeRefresh()
        observeViewModel()
        setupClickListeners()
        setupScrollToTopButton()


        return binding.root
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }

        binding.rvNotifications.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }

        binding.rvNotifications.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) { // scrolling down
                    binding.btnScrollTop.show()
                } else if (dy < 0) { // scrolling up
                    binding.btnScrollTop.hide()
                }
            }
        })

    }

    private fun setupFilterListener() {
        binding.filterRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedFilter = when (checkedId) {
                R.id.rb_all -> NotificationsViewModel.NotificationFilter.ALL
                R.id.rb_power -> NotificationsViewModel.NotificationFilter.POWER
                R.id.rb_door -> NotificationsViewModel.NotificationFilter.DOOR
                R.id.rb_temp -> NotificationsViewModel.NotificationFilter.TEMP
                R.id.rb_battery -> NotificationsViewModel.NotificationFilter.BATTERY
                else -> NotificationsViewModel.NotificationFilter.ALL
            }

            notificationsViewModel.setFilter(selectedFilter)
            binding.rvNotifications.scrollToPosition(0) // always scroll to top
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            notificationsViewModel.refreshNotifications()
        }
    }

    private fun observeViewModel() {
        notificationsViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            val groupedList = notificationsViewModel.groupNotificationsByDate(notifications)
            notificationAdapter.submitList(groupedList)
            binding.rvNotifications.scrollToPosition(0)
            updateUI(notifications)
        }

        notificationsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
            binding.progressBar.visibility =
                if (isLoading && notificationAdapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        notificationsViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                showErrorState(errorMessage)
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            } else {
                binding.layoutErrorState.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnRetry.setOnClickListener {
            notificationsViewModel.refreshNotifications()
        }
    }

    private fun updateUI(notifications: List<NotificationItem>) {
        when {
            notifications.isEmpty() -> showEmptyState()
            else -> showNotifications()
        }
    }

    private fun setupScrollToTopButton() {
        binding.btnScrollTop.setOnClickListener {
            binding.rvNotifications.smoothScrollToPosition(0)
        }
    }


    private fun showEmptyState() {
        binding.rvNotifications.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.layoutErrorState.visibility = View.GONE
    }

    private fun showNotifications() {
        binding.rvNotifications.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.GONE
    }

    private fun showErrorState(errorMessage: String) {
        binding.rvNotifications.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.GONE
        binding.layoutErrorState.visibility = View.VISIBLE
        binding.tvErrorMessage.text = errorMessage
    }

    private fun handleNotificationClick(notification: NotificationItem) {
        notificationsViewModel.markAsRead(notification.id)
        Toast.makeText(context, "Notification from ${notification.deviceName}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
