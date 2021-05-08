package com.haolin.swift.downloader.library.core.listener


interface IDownloadListener {
    fun onProgressChange(downloadBytes: Long, totalBytes: Long)
    fun onFinish(downloadBytes: Long, totalBytes: Long)
    fun onStop(downloadBytes: Long, totalBytes: Long)
}