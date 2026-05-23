package com.example.ui.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.DownloadRepository
import com.example.data.DownloadTask
import com.example.data.SniffeDatabase
import com.example.data.StorageType
import com.example.workers.DownloadWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class SniffeViewModel(application: android.app.Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        SniffeDatabase::class.java, "sniffe-db"
    ).fallbackToDestructiveMigration().build()
    
    private val repository = DownloadRepository(db.downloadDao())
    
    var lastBrowserUrl by mutableStateOf("https://google.com")
        private set
    
    fun updateBrowserUrl(url: String) {
        lastBrowserUrl = url
    }

    private val _bookmarks = MutableStateFlow<Set<String>>(emptySet())
    val bookmarks = _bookmarks.asStateFlow()
    
    fun toggleBookmark(url: String) {
        val current = _bookmarks.value.toMutableSet()
        if (current.contains(url)) current.remove(url) else current.add(url)
        _bookmarks.value = current
    }
    
    val tasks: StateFlow<List<DownloadTask>> = repository.allTasks.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )
    
    val storageUsage: StateFlow<Long?> = repository.totalStorageUsage.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0L
    )

    fun addTask(url: String, fileName: String, priority: Int, storageType: StorageType = StorageType.LOCAL_PHONE, quality: String = "Auto") {
        viewModelScope.launch {
            val task = DownloadTask(
                url = url, 
                fileName = fileName, 
                priority = priority,
                storageType = storageType,
                quality = quality
            )
            repository.insertTask(task)
            
            // Enqueue background worker
            val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(workDataOf("taskId" to task.id))
                .build()
                
            WorkManager.getInstance(getApplication()).enqueue(workRequest)
        }
    }
    
    fun updatePriority(task: DownloadTask, newPriority: Int) {
        viewModelScope.launch {
            repository.updateTask(task.copy(priority = newPriority))
        }
    }
    
    fun renameTask(task: DownloadTask, newName: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(fileName = newName))
        }
    }
    
    fun updateStorageType(task: DownloadTask, type: StorageType) {
        viewModelScope.launch {
            repository.updateTask(task.copy(storageType = type))
        }
    }
    
    fun deleteTask(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }
}
