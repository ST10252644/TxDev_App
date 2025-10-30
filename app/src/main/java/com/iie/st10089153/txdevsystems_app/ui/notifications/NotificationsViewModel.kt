package com.iie.st10089153.txdevsystems_app.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = NotificationsRepository(application)

    private val _filter = MutableLiveData(NotificationFilter.ALL)
    val filter: LiveData<NotificationFilter> = _filter

    private val _notifications = MutableLiveData<List<NotificationItem>>()
    val notifications: LiveData<List<NotificationItem>> = _notifications

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        loadNotifications()
    }

    fun setFilter(filter: NotificationFilter) {
        _filter.value = filter
        loadNotifications()
    }

    fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val allNotifications = repository.fetchAllNotifications()
                val filtered = when (_filter.value) {
                    NotificationFilter.ALL -> allNotifications
                    NotificationFilter.POWER -> allNotifications.filter { it.type == NotificationType.POWER_FAILURE }
                    NotificationFilter.DOOR -> allNotifications.filter { it.type == NotificationType.DOOR_OPEN }
                    NotificationFilter.TEMP -> allNotifications.filter {
                        it.type == NotificationType.TEMPERATURE_HIGH || it.type == NotificationType.TEMPERATURE_LOW
                    }
                    NotificationFilter.BATTERY -> allNotifications.filter { it.type == NotificationType.BATTERY_LOW }
                    else -> allNotifications
                }
                _notifications.value = filtered
            } catch (e: Exception) {
                _error.value = "Failed to load notifications: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshNotifications() {
        loadNotifications()
    }

    fun markAsRead(notificationId: String) {
        val currentNotifications = _notifications.value?.toMutableList() ?: return
        val index = currentNotifications.indexOfFirst { it.id == notificationId }
        if (index != -1) {
            currentNotifications[index] = currentNotifications[index].copy(isRead = true)
            _notifications.value = currentNotifications
        }
    }

    fun getUnreadCount(): Int {
        return _notifications.value?.count { !it.isRead } ?: 0
    }

    fun groupNotificationsByDate(notifications: List<NotificationItem>): List<NotificationListItem> {
        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val list = mutableListOf<NotificationListItem>()

        for (i in 0..6) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, -i)
            val dayDateStr = sdfDate.format(cal.time)
            val label = when (i) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> "$i days ago"
            }

            val dayNotifications = notifications.filter {
                try { sdfDate.format(sdfInput.parse(it.timestamp)!!) == dayDateStr }
                catch (e: Exception) { false }
            }.sortedByDescending { it.timestamp }

            if (dayNotifications.isNotEmpty()) {
                list.add(NotificationListItem.Header(label, dayDateStr))
                dayNotifications.forEach { list.add(NotificationListItem.Notification(it)) }
            }
        }
        return list
    }

    enum class NotificationFilter {
        ALL, POWER, DOOR, TEMP, BATTERY
    }
}
