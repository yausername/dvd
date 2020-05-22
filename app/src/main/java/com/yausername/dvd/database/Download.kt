package com.yausername.dvd.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "downloads_table")
data class Download(
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "total_size")
    var totalSize: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "downloaded_percent")
    var downloadedPercent: Double = 0.0

    @ColumnInfo(name = "downloaded_size")
    var downloadedSize: Long = 0L

    @ColumnInfo(name = "downloaded_path")
    lateinit var downloadedPath: String

    @ColumnInfo(name = "media_type")
    lateinit var mediaType: String
}