package com.msit.minorproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ImageHistory::class], version = 1, exportSchema = false)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageHistoryDao(): ImageHistoryDao

    companion object {
        private var instance: ImageDatabase? = null

        fun getInstance(context: Context): ImageDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): ImageDatabase {
            return Room.databaseBuilder(context, ImageDatabase::class.java, "image-history")
                .build()
        }
    }
}
