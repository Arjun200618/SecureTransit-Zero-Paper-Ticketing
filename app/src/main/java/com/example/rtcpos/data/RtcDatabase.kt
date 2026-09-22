package com.example.rtcpos.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rtcpos.data.dao.TicketDao
import com.example.rtcpos.data.dao.TripDao
import com.example.rtcpos.data.entity.TicketEntity
import com.example.rtcpos.data.entity.TripSessionEntity

@Database(
    entities = [
        TripSessionEntity::class,
        TicketEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RtcDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao
    abstract fun ticketDao(): TicketDao

    companion object {
        @Volatile
        private var INSTANCE: RtcDatabase? = null

        fun getDatabase(context: Context): RtcDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RtcDatabase::class.java,
                    "rtc_pos_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
