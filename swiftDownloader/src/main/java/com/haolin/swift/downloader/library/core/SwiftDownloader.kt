package com.haolin.swift.downloader.library.core

import android.content.*
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.haolin.swift.downloader.library.core.downloader.DefaultDownloader
import com.haolin.swift.downloader.library.core.downloader.ForegroundServiceDownloader
import com.haolin.swift.downloader.library.core.downloader.IDownloader
import com.haolin.swift.downloader.library.core.sender.DefaultNotificationSender
import com.haolin.swift.downloader.library.core.sender.NotificationSender
import com.haolin.swift.downloader.library.database.TaskInfo
import com.haolin.swift.downloader.library.tool.TAG

object SwiftDownloader {
    //设置
    val option by lazy { SwiftDownloaderOption() }

    private var realDownloader: IDownloader? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as ForegroundServiceDownloader.DownloadServiceBinder
            realDownloader = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: ")
        }
    }

    var onDownloadError: (Exception) -> Unit = {}
    var onDownloadProgressChange: (Long) -> Unit = {}
    var onDownloadSpeed: (String) -> Unit = {}
    var onDownloadStop: (Long, Long) -> Unit = { _: Long, _: Long -> }
    var onDownloadFinished: (String, String, Long) -> Unit = { _: String, _: String, _: Long -> }


    lateinit var notificationSender: NotificationSender

    fun initWithServiceMode(contextWrapper: ContextWrapper): SwiftDownloader {
        initSender(contextWrapper.applicationContext)
        val serviceIntent = Intent(contextWrapper, ForegroundServiceDownloader::class.java)
        contextWrapper.apply {
            startService(serviceIntent)
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        return this
    }

    private fun initSender(appContext: Context) {
//        if (!this::notificationSender.isInitialized) {
        notificationSender = DefaultNotificationSender(appContext)
        notificationSender.createNotificationChannel()
        //}
    }

    fun initWithDefaultMode(activity: FragmentActivity) {
        initSender(activity.applicationContext)
        realDownloader = ViewModelProvider(activity).get(DefaultDownloader::class.java)
    }

    fun initWithDefaultMode(fragment: Fragment) {
        initSender(fragment.activity?.applicationContext ?: fragment.requireContext())
        realDownloader = ViewModelProvider(fragment).get(DefaultDownloader::class.java)
    }

    fun close(contextWrapper: ContextWrapper) {
        val serviceIntent = Intent(contextWrapper, ForegroundServiceDownloader::class.java)
        realDownloader?.close()
        contextWrapper.apply {
            unbindService(serviceConnection)
            stopService(serviceIntent)
        }

    }

    fun enqueue(
        singleTask: Boolean,
        url: String,
        filePath: String,
        fileName: String
    ): SwiftDownloader {
        realDownloader?.enqueue(singleTask, url, filePath, fileName)
        return this
    }

    fun stopAll() {
        realDownloader?.stopAll()
    }

    fun resume() {
        realDownloader?.resumeAndStart()
    }

    fun deleteByUrl(url: String) {
        realDownloader?.deleteByUrl(url)
    }

    fun deleteAllTaskInfo() {
        realDownloader?.deleteAllTaskInfo()
    }

    fun cancelAll() {
        realDownloader?.cancelAll()
    }

    fun cancel() {
        realDownloader?.cancel()
    }

    fun clearCache(taskInfo: TaskInfo) {
        realDownloader?.clearCache(taskInfo)
    }

    fun setOnError(onError: (Exception) -> Unit): SwiftDownloader {
        onDownloadError = onError
        return this
    }

    fun setOnProgressChange(onProgressChange: (Long) -> Unit): SwiftDownloader {
        onDownloadProgressChange = onProgressChange
        return this
    }

    fun setOnDownloadSpeed(speed: (String) -> Unit): SwiftDownloader {
        onDownloadSpeed = speed
        return this
    }

    fun setOnStop(onStop: (Long, Long) -> Unit): SwiftDownloader {
        onDownloadStop = onStop
        return this
    }

    fun setOnFinished(onFinished: (String, String, Long) -> Unit): SwiftDownloader {
        onDownloadFinished = onFinished
        return this
    }


    /**
     * 获取当前下载队列所有任务信息的数组
     * @return Array<(TaskInfo?)>
     */
    fun getDownloadQueueArray() = realDownloader?.getDownloadQueueArray()

    /**
     * 查询所有任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryAllTaskInfo(): MutableList<TaskInfo> = realDownloader!!.queryAllTaskInfo()

    /**
     * 查询未完成的任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryUnfinishedTaskInfo(): MutableList<TaskInfo> =
        realDownloader!!.queryUnfinishedTaskInfo()

    /**
     * 返回包含所有任务信息的LiveData
     * @return LiveData<List<TaskInfo>>
     */
    fun getAllTaskInfoLiveData() = realDownloader?.getAllTaskInfoLiveData()

    /**
     * 返回包含未完成的任务信息的LiveData
     * @return LiveData<MutableList<TaskInfo>>
     */
    fun getUnfinishedTaskInfoLiveData() = realDownloader?.getUnfinishedTaskInfoLiveData()

    /**
     *查询已完成的任务信息
     * @return MutableList<TaskInfo>
     */
    suspend fun queryFinishedTaskInfo() = realDownloader?.queryFinishedTaskInfo()

    /**
     * 返回包含已完成的任务信息的LiveData
     * @return LiveData<MutableList<TaskInfo>>
     */
    fun getFinishedTaskInfoLiveData() = realDownloader?.getFinishedTaskInfoLiveData()

}