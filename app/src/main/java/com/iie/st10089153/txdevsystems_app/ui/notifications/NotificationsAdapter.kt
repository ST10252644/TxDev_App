package com.iie.st10089153.txdevsystems_app.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.iie.st10089153.txdevsystems_app.R
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsAdapter(
    private val onClick: (Notification) -> Unit
) : RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder>() {

    private var notifications: List<Notification> = emptyList()

    fun submitList(list: List<Notification>) {
        notifications = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        val notification = notifications[position]
        holder.bind(notification, onClick)
    }

    override fun getItemCount(): Int = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.notification_title)
        private val message: TextView = itemView.findViewById(R.id.notification_message)
        private val time: TextView = itemView.findViewById(R.id.notification_time)

        fun bind(notification: Notification, onClick: (Notification) -> Unit) {
            title.text = notification.title
            message.text = notification.message
            val dateFormat = SimpleDateFormat("HH:mm, dd MMM", Locale.getDefault())
            time.text = dateFormat.format(notification.timestamp)

            itemView.setOnClickListener { onClick(notification) }
        }
    }
}