package com.iie.st10089153.txdevsystems_app.ui.notifications

sealed class NotificationListItem {
    data class Header(val label: String, val date: String) : NotificationListItem()

    data class Notification(val item: NotificationItem) : NotificationListItem()
}
