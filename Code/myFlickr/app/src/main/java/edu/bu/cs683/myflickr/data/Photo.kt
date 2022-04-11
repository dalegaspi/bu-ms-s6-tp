package edu.bu.cs683.myflickr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String
)
