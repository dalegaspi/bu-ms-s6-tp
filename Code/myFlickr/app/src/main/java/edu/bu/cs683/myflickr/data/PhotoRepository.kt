package edu.bu.cs683.myflickr.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.Room
import java.lang.IllegalStateException
import java.util.concurrent.Executors

/**
 * Repository pattern recommended by Google per Big Nerd Ranch Ed. 4 pg. 227
 */
class PhotoRepository private constructor(context: Context) {

    private val database: PhotoDatabase = Room.databaseBuilder(
        context.applicationContext,
        PhotoDatabase::class.java,
        DATABASE_NAME
    ).build()

    private val photoDao = database.photoDao()

    companion object {
        val DATABASE_NAME = "photos-db"
        private var INSTANCE: PhotoRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = PhotoRepository(context)
            }
        }

        fun get(): PhotoRepository {
            return INSTANCE
                ?: throw IllegalStateException("PhotoRepository must be initialized")
        }
    }

    val executor = Executors.newSingleThreadExecutor()

    fun deleteAll() {
        database.clearAllTables()
    }

    fun add(photo: Photo) {
        executor.execute {
            photoDao.addProject(photo)
        }
    }

    fun delete(photo: Photo) {
        executor.execute {
            photoDao.delProject(photo)
        }
    }

    fun edit(photo: Photo) {
        executor.execute {
            photoDao.editProject(photo)
        }
    }

    fun getAll(): LiveData<List<Photo>> {
        return photoDao.getAllProjects()
    }

    fun count(): LiveData<Int> {
        return photoDao.count()
    }
}
