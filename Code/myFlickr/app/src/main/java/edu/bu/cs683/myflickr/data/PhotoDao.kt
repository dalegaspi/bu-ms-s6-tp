package edu.bu.cs683.myflickr.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addProject(photo: Photo)

    @Delete
    fun delProject(photo: Photo)

    @Update
    fun editProject(photo: Photo)

    @Query("SELECT count(*) From photos")
    fun count(): LiveData<Int>

    @Query("SELECT * FROM photos")
    fun getAllProjects(): LiveData<List<Photo>>
}
