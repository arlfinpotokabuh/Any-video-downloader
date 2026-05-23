package com.example.workers

import android.content.Context
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.DownloadStatus
import com.example.data.SniffeDatabase
import kotlinx.coroutines.delay

class DownloadWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getString("taskId") ?: return Result.failure()
        
        val db = Room.databaseBuilder(
            applicationContext,
            SniffeDatabase::class.java, "sniffe-db"
        ).fallbackToDestructiveMigration().build()
        
        val dao = db.downloadDao()
        var task = dao.getNextQueuedTask() 
        if (task == null || task.id != taskId) {
            // Task might have been prioritized over, just gracefully succeed or mock logic
            return Result.success()
        }
        
        // Start actual download
        task = task!!.copy(status = DownloadStatus.DOWNLOADING)
        dao.updateTask(task)

        val externalDir = applicationContext.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
        if (externalDir == null) {
            task = task!!.copy(status = DownloadStatus.FAILED)
            dao.updateTask(task)
            return Result.failure()
        }
        val outputFile = java.io.File(externalDir, task.fileName)

        var connection: java.net.HttpURLConnection? = null
        var inputStream: java.io.InputStream? = null
        var outputStream: java.io.FileOutputStream? = null

        try {
            val url = java.net.URL(task.url)
            connection = url.openConnection() as java.net.HttpURLConnection
            connection.connect()

            if (connection.responseCode != java.net.HttpURLConnection.HTTP_OK) {
                task = task!!.copy(status = DownloadStatus.FAILED)
                dao.updateTask(task)
                return Result.failure()
            }

            val fileLength = connection.contentLength.toLong()
            task = task!!.copy(totalBytes = if (fileLength > 0) fileLength else 0)
            dao.updateTask(task)

            inputStream = connection.inputStream
            outputStream = java.io.FileOutputStream(outputFile)

            val data = ByteArray(4096)
            var total: Long = 0
            var count: Int
            var lastUpdate = System.currentTimeMillis()

            while (inputStream.read(data).also { count = it } != -1) {
                total += count.toLong()
                outputStream.write(data, 0, count)

                if (System.currentTimeMillis() - lastUpdate > 500) {
                    task = task!!.copy(downloadedBytes = total)
                    dao.updateTask(task)
                    lastUpdate = System.currentTimeMillis()
                }
            }

            outputStream.flush()
            task = task!!.copy(status = DownloadStatus.COMPLETED, downloadedBytes = total, localFilePath = outputFile.absolutePath)
            dao.updateTask(task)

        } catch (e: Exception) {
            e.printStackTrace()
            task = task!!.copy(status = DownloadStatus.FAILED)
            dao.updateTask(task)
            return Result.failure()
        } finally {
            try {
                outputStream?.close()
                inputStream?.close()
            } catch (e: Exception) {}
            connection?.disconnect()
        }
        
        return Result.success()
    }
}
