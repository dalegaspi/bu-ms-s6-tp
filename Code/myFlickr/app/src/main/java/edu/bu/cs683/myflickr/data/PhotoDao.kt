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

    @Query("select count(*) From photos")
    fun count(): Int

    @Query("select * from photos")
    fun getAllPhotos(): List<Photo>

    @Query("select camera, count(*) as counts from photos group by camera limit 5")
    fun getGroupCountsByCamera(): List<CameraCounts>
}

data class CameraCounts(var camera: String, var counts: Int)