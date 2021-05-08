package com.haolin.swift.downloader.library.http

import com.haolin.swift.downloader.library.core.SwiftDownloaderOption
import com.haolin.swift.downloader.library.core.controller.DownloadController
import com.haolin.swift.downloader.library.core.listener.IDownloadListener
import com.haolin.swift.downloader.library.database.TaskInfo
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit


object OkHttpManager {

    fun getClient(
        option: SwiftDownloaderOption,
        downloadListener: IDownloadListener,
        downloadController: DownloadController
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(ProgressInterceptor(downloadListener, downloadController))
            .connectTimeout(option.timeout, TimeUnit.SECONDS)
            .build()


    fun createRequest(taskInfo: TaskInfo): Request =
        Request.Builder().url(taskInfo.url)
            .addHeader("Range", "bytes=${taskInfo.downloadedBytes}-")
            .build()
}