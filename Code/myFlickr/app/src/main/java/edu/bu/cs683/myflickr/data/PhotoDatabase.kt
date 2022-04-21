package edu.bu.cs683.myflickr.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Photo::class],
    version = 5
)

abstract class PhotoDatabase : RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}
