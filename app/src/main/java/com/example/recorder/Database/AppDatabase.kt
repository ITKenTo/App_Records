package com.example.recorder.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.recorder.Dao.AudioRecorDao
import com.example.recorder.Model.AudioRecord


@Database(entities = [AudioRecord::class], version = 1, exportSchema = true)
abstract class AppDatabase:RoomDatabase() {
    abstract fun  audiorecord():AudioRecorDao
    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            if (INSTANCE == null) {
                synchronized(this) {
                    // Pass the database to the INSTANCE
                    INSTANCE = buildDatabase(context)
                }
            }
            // Return database.
            return INSTANCE!!
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "record"
            )
                .build()
        }
    }
}