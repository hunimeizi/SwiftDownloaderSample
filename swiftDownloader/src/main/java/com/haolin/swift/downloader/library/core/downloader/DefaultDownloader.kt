package com.haolin.swift.downloader.library.core.downloader

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.*
import com.haolin.swift.downloader.library.core.SwiftDownloader
import com.haolin.swift.downloader.library.core.controller.DownloadController
import com.haolin.swift.downloader.library.core.listener.IDownloadListener
import com.haolin.swift.downloader.library.database.DownloadTaskManager
import com.haolin.swift.downloader.library.database.TaskInfo
import com.haolin.swift.downloader.library.http.OkHttpManager
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import java.util.concurrent.ConcurrentLinkedQueue

class DefaultDownloader(application: Application) : IDownloader, AndroidViewModel(application) {
    override var appContext: Context = application.applicationContext
    override val scope: CoroutineScope = viewModelScope
    override val downloadController: DownloadController by lazy { DownloadController() }
    override val downloadQueue: ConcurrentLinkedQueue<TaskInfo> by lazy { ConcurrentLinkedQueue<TaskInfo>() }
    override val taskManager: DownloadTaskManager = DownloadTaskManager(appContext)
    override var downloadingTask: TaskInfo? = null
    override val okHttpClient: OkHttpClient by lazy {
        OkHttpManager.getClient(SwiftDownloader.option, downloadListener, downloadController)
    }
    override val downloadListener: IDownloadListener by lazy { createListener() }

    override fun close() {
        stopAll()
        downloadingTask = null
        SwiftDownloader.notificationSender.cancelDownloadProgressNotification()
    }

    override fun onCleared() {
        super.onCleared()
        close()
        Toast.makeText(getApplication(),"已销毁",Toast.LENGTH_SHORT).show()
    }


}