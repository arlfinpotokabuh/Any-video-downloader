package com.example.data

import kotlinx.coroutines.flow.Flow

class DownloadRepository(private val dao: DownloadDao) {
    val allTasks: Flow<List<DownloadTask>> = dao.getAllTasks()
    val totalStorageUsage: Flow<Long?> = dao.getTotalStorageUsage()

    suspend fun insertTask(task: DownloadTask) = dao.insertTask(task)
    suspend fun updateTask(task: DownloadTask) = dao.updateTask(task)
    suspend fun deleteById(id: String) = dao.deleteById(id)
}
