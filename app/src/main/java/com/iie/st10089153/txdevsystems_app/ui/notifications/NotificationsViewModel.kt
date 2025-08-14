// NotificationsViewModel.kt - ViewModel for notifications
package com.iie.st10089153.txdevsystems_app.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Date

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> = _notifications

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        // Sample notifications - replace with your data source
        val sampleNotifications = listOf(
            Notification(
                id = "1",
                title = "Welcome!",
                message = "Welcome to TxDevSystems App",
                timestamp = Date(),
                type = NotificationType.GENERAL
            ),
            Notification(
                id = "2",
                title = "System Update",
                message = "New features are now available",
                timestamp = Date(System.currentTimeMillis() - 3600000), // 1 hour ago
                type = NotificationType.UPDATE
            ),
            Notification(
                id = "3",
                title = "Alert",
                message = "Important system maintenance scheduled",
                timestamp = Date(System.currentTimeMillis() - 7200000), // 2 hours ago
                type = NotificationType.ALERT
            )
        )

        _notifications.value = sampleNotifications
    }

    fun markAsRead(notificationId: String) {
        val currentList = _notifications.value ?: return
        val updatedList = currentList.map { notification ->
            if (notification.id == notificationId) {
                notification.copy(isRead = true)
            } else {
                notification
            }
        }
        _notifications.value = updatedList
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }
}