package com.iie.st10089153.txdevsystems_app.database

import androidx.room.*
import com.iie.st10089153.txdevsystems_app.database.entities.*
import com.iie.st10089153.txdevsystems_app.database.dao.*

@Database(
    entities = [
        CachedUnit::class,
        CachedDashboard::class,
        CachedConfig::class,
        CachedNotification::class,
        CachedRangeData::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun unitDao(): UnitDao
    abstract fun dashboardDao(): DashboardDao
    abstract fun configDao(): ConfigDao
    abstract fun notificationDao(): NotificationDao
    abstract fun rangeDataDao(): RangeDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "txdev_cache_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}