package com.haolin.swift.downloader.library.core.downloader

import android.content.Context
import android.util.Log
import com.haolin.swift.downloader.library.core.SwiftDownloader
import com.haolin.swift.downloader.library.core.controller.DownloadController
import com.haolin.swift.downloader.library.core.listener.IDownloadListener
import com.haolin.swift.downloader.library.database.*
import com.haolin.swift.downloader.library.http.OkHttpManager
import com.haolin.swift.downloader.library.tool.MediaStoreHelper
import com.haolin.swift.downloader.library.tool.TAG
import com.haolin.swift.downloader.library.tool.writeFileInDisk
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import java.io.File
import java.util.Queue

interface IDownloader {
    var appContext: Context

    val scope: CoroutineScope
    val downloadController: DownloadController
    val downloadQueue: Queue<TaskInfo>
    val taskManager: DownloadTaskManager
    var downloadingTask: TaskInfo?
    val okHttpClient: OkHttpClient
    val downloadListener: IDownloadListener
    fun enqueue(url: String, filePath: String, fileName: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val taskInfo = TaskInfo(
                    System.currentTimeMillis(),
                    fileName,
                    filePath,
                    url,
                    0,
                    0,
                    TASK_STATUS_UNINITIALIZED
                )
                taskManager.insertTaskInfo(taskInfo)
                downloadQueue.offer(taskInfo)
                if (downloadingTask == null) switching2NextTask()

            } catch (e: Exception) {
                Log.e("download", e.localizedMessage, e)
                withContext(Dispatchers.Main) {
                    SwiftDownloader.onDownloadError(e)
                }
            }
        }
    }

    private suspend fun download() {
        withContext(Dispatchers.IO) {
            if (downloadingTask == null) {
                return@withContext
            }
            val task = downloadingTask!!
            val request = OkHttpManager.createRequest(task)
            val response = okHttpClient.newCall(request).execute()
            writeFileInDisk(
                response.body()!!,
                File(task.filePath, task.fileName),
                task.status == TASK_STATUS_UNFINISHED
            )
            Log.d(TAG, "download: switching2NextTask()")
            switching2NextTask()
        }
    }

    private suspend fun switching2NextTask() {
        downloadingTask = downloadQueue.poll()
        downloadController.start()
        download()
    }

    fun stopAll() {
        downloadController.pause()
        downloadQueue.clear()
    }

    fun resumeAndStart() {
        scope.launch(Dispatchers.IO) {
            queryUnfinishedTaskInfo().let {
                if (it.isNotEmpty()) {
                    downloadQueue.clear()
                    downloadQueue.addAll(it)
                    downloadController.start()
                    switching2NextTask()
                }
            }
        }
    }


    fun cancelAll() {
        downloadController.pause()
        scope.launch(Dispatchers.IO) {
            Log.d(TAG, "cancelAll deleteTaskInfoArray: ${getDownloadQueueArray()}")
            taskManager.deleteAllUnfinishedTaskInfo()
            downloadQueue.forEach {
                clearCache(it)
            }
            downloadQueue.clear()
            downloadingTask = null
            //downloadController.start()
        }
        SwiftDownloader.notificationSender.cancelDownloadProgressNotification()

    }

    fun cancel() {
        downloadController.pause()
        if (downloadingTask != null) {
            scope.launch(Dispatchers.IO) {
                Log.d(TAG, "delete: $downloadingTask")
                clearCache(downloadingTask!!)
                taskManager.deleteTaskInfoByID(downloadingTask!!.id)
                downloadQueue.poll()
                downloadingTask = null
                SwiftDownloader.notificationSender.cancelDownloadProgressNotification()
                delay(2000)
                //downloadController.start()
                switching2NextTask()
            }
        }
    }

    private fun notifyMediaStore(taskInfo: TaskInfo) {
        try {
            MediaStoreHelper.notifyMediaStore(taskInfo, appContext)
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "notifyMediaStore: ${e.message}", e)
            SwiftDownloader.onDownloadError(e)
        }
    }

    fun clearCache(taskInfo: TaskInfo) {
        scope.launch(Dispatchers.IO) {
            val file = File(taskInfo.getAbsolutePath())
            if (file.exists()) file.delete()
        }
    }

    fun getDownloadQueueArray() = downloadQueue.toTypedArray()


    suspend fun queryAllTaskInfo(): MutableList<TaskInfo> = taskManager.getAllTaskInfo()

    suspend fun queryUnfinishedTaskInfo(): MutableList<TaskInfo> =
        taskManager.getUnfinishedTaskInfo()


    fun getAllTaskInfoLiveData() = taskManager.getAllTaskInfoLiveData()


    fun getUnfinishedTaskInfoLiveData() = taskManager.getUnfinishedTaskInfoLiveData()


    suspend fun queryFinishedTaskInfo() = taskManager.getFinishedTaskInfo()


    fun getFinishedTaskInfoLiveData() = taskManager.getFinishedTaskInfoLiveData()

//    fun setOnError(onError: (Exception) -> Unit) {
//        SwiftDownloader.onDownloadError = onError
//    }
//
//
//    fun setOnProgressChange(onProgressChange: (Long) -> Unit) {
//        SwiftDownloader.onDownloadProgressChange = onProgressChange
//    }
//
//    fun setOnStop(onStop: (Long, Long) -> Unit) {
//        SwiftDownloader.onDownloadStop = onStop
//    }
//
//    fun setOnFinished(onFinished: (String, String) -> Unit) {
//        SwiftDownloader.onDownloadFinished = onFinished
//    }
//
//    fun setNotificationSender(sender: NotificationSender) {
//        SwiftDownloader.notificationSender = sender
//    }

    fun createListener(): IDownloadListener = object : IDownloadListener {
        var progress = 0L
        override fun onProgressChange(downloadBytes: Long, totalBytes: Long) {
            val newProgress =
                (downloadingTask!!.downloadedBytes + downloadBytes) * 100 / if (downloadingTask!!.status == TASK_STATUS_UNINITIALIZED) totalBytes else downloadingTask!!.totalBytes
            if (progress != newProgress && newProgress < 100L) {
                progress = newProgress
                Log.d(TAG, "$progress %")
                if (SwiftDownloader.option.showNotification) {
                    SwiftDownloader.notificationSender.showDownloadProgressNotification(
                        progress.toInt(), downloadingTask?.fileName ?: "null"
                    )
                }
                scope.launch(Dispatchers.Main) {
                    SwiftDownloader.onDownloadProgressChange(progress)
                }
            } else if (progress != newProgress && newProgress == 100L) {
                onFinish(downloadBytes, totalBytes)
            }
        }

        override fun onStop(downloadBytes: Long, totalBytes: Long) {
            Log.d(TAG, "$downloadBytes b")
            val task = downloadingTask
            task?.let {
                it.downloadedBytes += downloadBytes
                if (it.status == TASK_STATUS_UNINITIALIZED) {
                    it.totalBytes = totalBytes
                    it.status = TASK_STATUS_UNFINISHED
                }
                scope.launch(Dispatchers.IO) { taskManager.updateTaskInfo(it) }
                SwiftDownloader.notificationSender.showDownloadStopNotification(task.fileName)
            }
            scope.launch(Dispatchers.Main) {
                SwiftDownloader.onDownloadStop(downloadBytes, totalBytes)
            }
        }

        override fun onFinish(downloadBytes: Long, totalBytes: Long) {
            Log.d(TAG, "onFinish: ")
            val task = downloadingTask
            task?.let {
                if (it.status == TASK_STATUS_UNINITIALIZED) it.totalBytes = totalBytes
                it.downloadedBytes += downloadBytes
                it.status = TASK_STATUS_FINISH
                scope.launch(Dispatchers.IO) {
                    taskManager.insertTaskInfo(it)
                    if (SwiftDownloader.option.notifyMediaStoreWhenItDone) {
                        notifyMediaStore(it)
                    }
                }

            }
            if (SwiftDownloader.option.showNotification) {
                SwiftDownloader.notificationSender.showDownloadDoneNotification(
                    downloadingTask?.fileName ?: "null",
                    downloadingTask?.filePath ?: "null"
                )
                SwiftDownloader.notificationSender.cancelDownloadProgressNotification()
            }
            scope.launch(Dispatchers.Main) {
                SwiftDownloader.onDownloadFinished(
                    task?.filePath ?: "null",
                    task?.fileName ?: "null"
                )
            }
        }
    }
    fun close()

}