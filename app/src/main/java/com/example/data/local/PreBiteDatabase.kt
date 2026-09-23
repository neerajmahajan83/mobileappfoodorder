package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MenuItemEntity::class,
        OrderEntity::class,
        SupportTicketEntity::class,
        MealSubscriptionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PreBiteDatabase : RoomDatabase() {
    abstract fun dao(): PreBiteDao

    companion object {
        @Volatile
        private var INSTANCE: PreBiteDatabase? = null

        fun getDatabase(context: Context): PreBiteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PreBiteDatabase::class.java,
                    "prebite_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
