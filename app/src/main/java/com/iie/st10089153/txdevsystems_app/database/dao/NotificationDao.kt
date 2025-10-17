package com.iie.st10089153.txdevsystems_app.database.dao
import com.iie.st10089153.txdevsystems_app.database.entities.CachedNotification

import androidx.room.*

@Dao
interface NotificationDao {
    @Query("SELECT * FROM cached_notifications ORDER BY notification_timestamp DESC")
    suspend fun getAllNotifications(): List<CachedNotification>

    @Query("SELECT * FROM cached_notifications WHERE type = :type ORDER BY notification_timestamp DESC")
    suspend fun getNotificationsByType(type: String): List<CachedNotification>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<CachedNotification>)

    @Query("UPDATE cached_notifications SET is_read = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Query("DELETE FROM cached_notifications WHERE cached_at < :expiredBefore")
    suspend fun deleteExpired(expiredBefore: Long)

    @Query("DELETE FROM cached_notifications")
    suspend fun clearAll()
}