package com.example.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadTask::class], version = 3, exportSchema = false)
abstract class SniffeDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao
}
