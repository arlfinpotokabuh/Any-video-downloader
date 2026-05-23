package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "download_tasks")
data class DownloadTask(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val url: String,
    val fileName: String,
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val priority: Int = 1,
    val timestamp: Long = System.currentTimeMillis(),
    val storageType: StorageType = StorageType.LOCAL_PHONE,
    val quality: String = "Auto",
    val localFilePath: String? = null
)

enum class DownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, SYNCING
}

enum class StorageType {
    G_DRIVE, LOCAL_PHONE
}
