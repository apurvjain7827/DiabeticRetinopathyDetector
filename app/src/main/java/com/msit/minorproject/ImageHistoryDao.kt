package com.msit.minorproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ImageHistoryDao {
    @Insert
    suspend fun insert(imageHistory: ImageHistory)

    @Query("SELECT * FROM image_history")
    suspend fun getAll(): List<ImageHistory>

    @Query("SELECT * FROM image_history LIMIT 1")
    suspend fun getAny(): ImageHistory?
}

