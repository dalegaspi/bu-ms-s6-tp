package edu.bu.cs683.myflickr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class Photo(
    @PrimaryKey
    val id: String,
    val url: String,
    val title: String,
    val isPublic: Boolean = true,
    val description: String? = null,
    val camera: String? = null,
    val lens: String? = null,
    val aperture: String? = null,
    val flash: String? = null,
    val shutterSpeed: String? = null,
    val whiteBalance: String? = null
)
