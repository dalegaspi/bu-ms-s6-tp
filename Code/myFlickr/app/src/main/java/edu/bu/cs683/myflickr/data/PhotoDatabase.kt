package edu.bu.cs683.myflickr.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Photo::class],
    version = 1
)

abstract class PhotoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}