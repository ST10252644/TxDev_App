package com.iie.st10089153.txdevsystems_app.ui.notifications

import java.util.Date

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Date,
    val type: NotificationType,
    val isRead: Boolean = false
)