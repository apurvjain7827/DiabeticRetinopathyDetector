package com.msit.minorproject

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "image_history")
data class ImageHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val imagePath: String,
    val result: String,
    val timestamp: Long
)
