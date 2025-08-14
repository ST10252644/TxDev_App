package com.iie.st10089153.txdevsystems_app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.iie.st10089153.txdevsystems_app.databinding.FragmentNotificationsBinding

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var notificationsAdapter: NotificationsAdapter
    private lateinit var notificationsViewModel: NotificationsViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        notificationsViewModel =
            ViewModelProvider(this)[NotificationsViewModel::class.java]

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeNotifications()

        return binding.root
    }

    private fun setupRecyclerView() {
        notificationsAdapter = NotificationsAdapter { notification ->
            handleNotificationClick(notification)
        }

        binding.notificationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notificationsAdapter
        }
    }

    private fun observeNotifications() {
        notificationsViewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications.isEmpty()) {
                binding.emptyNotificationsMessage.visibility = View.VISIBLE
                binding.notificationsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyNotificationsMessage.visibility = View.GONE
                binding.notificationsRecyclerView.visibility = View.VISIBLE
                notificationsAdapter.submitList(notifications)
            }
        }
    }

    private fun handleNotificationClick(notification: Notification) {
        notificationsViewModel.markAsRead(notification.id)
        // You can also trigger navigation to a details page here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}