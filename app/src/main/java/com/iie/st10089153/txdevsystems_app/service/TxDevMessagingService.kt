package com.iie.st10089153.txdevsystems_app.service
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.iie.st10089153.txdevsystems_app.MainActivity
import com.iie.st10089153.txdevsystems_app.R
import com.iie.st10089153.txdevsystems_app.ui.notifications.NotificationType

class TxDevMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "TxDevFCM"
        private const val CHANNEL_ID = "temperature_alerts"
        private const val CHANNEL_NAME = "Temperature Alerts"
    }

    /**
     * Called when FCM token is generated or refreshed
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Token: $token")

        // Save token locally
        saveTokenLocally(token)

        // Send to backend
        sendTokenToBackend(token)
    }

    /**
     * Called when FCM message is received
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "Message received from: ${remoteMessage.from}")

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data: ${remoteMessage.data}")
            handleDataMessage(remoteMessage.data)
        }

        // Handle notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification: ${it.title} - ${it.body}")
            // If your backend sends notification payload, it will auto-display
            // But we handle it manually for more control
            sendNotification(
                title = it.title ?: "TXDev Alert",
                message = it.body ?: "",
                data = remoteMessage.data
            )
        }
    }

    /**
     * Handle custom data payload from backend
     * This maps to your existing NotificationType enum
     */
    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data["notification_type"] ?: "unknown"
        val deviceName = data["device_name"] ?: "Unknown Device"
        val imei = data["imei"] ?: ""
        val timestamp = data["timestamp"] ?: ""

        when (notificationType) {
            "TEMPERATURE_HIGH" -> {
                val temp = data["temperature"] ?: "N/A"
                val maxTemp = data["temp_max"] ?: "N/A"
                sendNotification(
                    title = "🔥 High Temperature Alert!",
                    message = "$deviceName: ${temp}°C (Max: ${maxTemp}°C)",
                    data = data,
                    priority = NotificationCompat.PRIORITY_HIGH,
                    notificationType = NotificationType.TEMPERATURE_HIGH
                )
            }

            "TEMPERATURE_LOW" -> {
                val temp = data["temperature"] ?: "N/A"
                val minTemp = data["temp_min"] ?: "N/A"
                sendNotification(
                    title = "❄️ Low Temperature Alert!",
                    message = "$deviceName: ${temp}°C (Min: ${minTemp}°C)",
                    data = data,
                    priority = NotificationCompat.PRIORITY_HIGH,
                    notificationType = NotificationType.TEMPERATURE_LOW
                )
            }

            "DOOR_OPEN" -> {
                sendNotification(
                    title = "🚪 Door Opened!",
                    message = "$deviceName door has been opened",
                    data = data,
                    priority = NotificationCompat.PRIORITY_DEFAULT,
                    notificationType = NotificationType.DOOR_OPEN
                )
            }

            "POWER_FAILURE" -> {
                val status = data["supply_status"] ?: "Power Failure"
                sendNotification(
                    title = "⚡ Power Alert!",
                    message = "$deviceName: $status",
                    data = data,
                    priority = NotificationCompat.PRIORITY_HIGH,
                    notificationType = NotificationType.POWER_FAILURE
                )
            }

            "BATTERY_LOW" -> {
                val voltage = data["voltage"] ?: "N/A"
                sendNotification(
                    title = "🔋 Low Battery!",
                    message = "$deviceName: ${voltage}V",
                    data = data,
                    priority = NotificationCompat.PRIORITY_DEFAULT,
                    notificationType = NotificationType.BATTERY_LOW
                )
            }

            else -> {
                Log.w(TAG, "Unknown notification type: $notificationType")
            }
        }
    }

    /**
     * Create and display notification
     */
    private fun sendNotification(
        title: String,
        message: String,
        data: Map<String, String>,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        notificationType: NotificationType? = null
    ) {
        // Create intent to open app on notification click
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("open_notifications", true)
            putExtra("device_imei", data["imei"])
            putExtra("notification_type", notificationType?.name)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Create notification channel
        createNotificationChannel()

        // Get icon based on notification type
        val iconRes = when (notificationType) {
            NotificationType.DOOR_OPEN -> R.drawable.ic_door_open
            NotificationType.TEMPERATURE_HIGH, NotificationType.TEMPERATURE_LOW -> R.drawable.ic_temperature
            NotificationType.POWER_FAILURE -> R.drawable.ic_power
            NotificationType.BATTERY_LOW -> R.drawable.ic_battery_low
            else -> R.drawable.ic_notification // default icon
        }

        // Build notification
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(iconRes)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Add vibration for high priority
        if (priority == NotificationCompat.PRIORITY_HIGH) {
            notificationBuilder.setVibrate(longArrayOf(1000, 1000, 1000))
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    /**
     * Create notification channel for Android 8+
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Temperature and device alerts"
                enableVibration(true)
                vibrationPattern = longArrayOf(1000, 1000, 1000)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Save FCM token locally
     */
    private fun saveTokenLocally(token: String) {
        getSharedPreferences("txdev_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("fcm_token", token)
            .apply()
        Log.d(TAG, "Token saved locally")
    }

    /**
     * Send FCM token to your backend
     */
    private fun sendTokenToBackend(token: String) {
        // TODO: Implement API call to your backend
        Log.d(TAG, "TODO: Send token to backend: $token")

        // You'll need to add this endpoint to your API
        // Example:
        /*
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val api = RetrofitClient.getApi(applicationContext)
                val response = api.registerFcmToken(
                    FcmTokenRequest(token = token)
                )
                if (response.isSuccessful) {
                    Log.d(TAG, "Token registered successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error sending token", e)
            }
        }
        */
    }
}