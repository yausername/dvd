package org.yausername.dvd.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DownloadsDao {
    @Insert
    suspend fun insert(item: Download)

    @Update
    suspend fun update(item: Download)

    @Delete
    suspend fun delete(item: Download)

    @Query("SELECT * from downloads_table WHERE id = :id")
    fun getById(id: Long): Download

    @Query("SELECT * from downloads_table ORDER BY timestamp DESC")
    fun getAllDownloads(): LiveData<List<Download>>

}