package com.iie.st10089153.txdevsystems_app.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onNotificationClick: (NotificationItem) -> Unit
) : ListAdapter<NotificationListItem, RecyclerView.ViewHolder>(NotificationDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_NOTIFICATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is NotificationListItem.Header -> TYPE_HEADER
            is NotificationListItem.Notification -> TYPE_NOTIFICATION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            NotificationViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is NotificationListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is NotificationListItem.Notification -> (holder as NotificationViewHolder).bind(item.item)
        }
    }

    inner class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconImageView: ImageView = itemView.findViewById(R.id.iv_notification_icon)
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_notification_title)
        private val messageTextView: TextView = itemView.findViewById(R.id.tv_notification_message)
        private val timestampTextView: TextView =
            itemView.findViewById(R.id.tv_notification_timestamp)
        private val deviceNameTextView: TextView = itemView.findViewById(R.id.tv_device_name)

        fun bind(notification: NotificationItem) {
            titleTextView.text = notification.title
            messageTextView.text = notification.message
            deviceNameTextView.text = notification.deviceName

            // Format timestamp
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            timestampTextView.text = try {
                val inputFormat =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                val date = inputFormat.parse(notification.timestamp)
                date?.let { outputFormat.format(it) } ?: notification.timestamp
            } catch (e: Exception) {
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val date = inputFormat.parse(notification.timestamp)
                    date?.let { outputFormat.format(it) } ?: notification.timestamp
                } catch (e2: Exception) {
                    notification.timestamp
                }
            }

            // Set icon & background based on type
            val (icon, colorRes) = when (notification.type) {
                NotificationType.DOOR_OPEN -> R.drawable.ic_door_open to android.R.color.holo_orange_dark
                NotificationType.TEMPERATURE_HIGH -> R.drawable.ic_temperature to android.R.color.holo_red_light
                NotificationType.TEMPERATURE_LOW -> R.drawable.ic_temperature to android.R.color.holo_blue_light
                NotificationType.POWER_FAILURE -> R.drawable.ic_power to android.R.color.holo_purple
                NotificationType.BATTERY_LOW -> R.drawable.ic_battery_low to android.R.color.holo_red_dark
            }

            iconImageView.setImageResource(icon)
            iconImageView.background =
                ContextCompat.getDrawable(itemView.context, R.drawable.circle_background)
            iconImageView.background.setTint(ContextCompat.getColor(itemView.context, colorRes))

            itemView.setOnClickListener {
                onNotificationClick(notification)
            }
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val labelText: TextView = itemView.findViewById(R.id.tvHeaderLabel)
        private val dateText: TextView = itemView.findViewById(R.id.tvHeaderDate)

        fun bind(header: NotificationListItem.Header) {
            labelText.text = (header as? NotificationListItem.Header)?.label ?: ""
            dateText.text = (header as? NotificationListItem.Header)?.date ?: ""
        }
    }

}

class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationListItem>() {
    override fun areItemsTheSame(oldItem: NotificationListItem, newItem: NotificationListItem): Boolean {
        return when {
            oldItem is NotificationListItem.Header && newItem is NotificationListItem.Header -> oldItem.date == newItem.date
            oldItem is NotificationListItem.Notification && newItem is NotificationListItem.Notification -> oldItem.item.id == newItem.item.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: NotificationListItem, newItem: NotificationListItem): Boolean {
        return oldItem == newItem
    }
}
