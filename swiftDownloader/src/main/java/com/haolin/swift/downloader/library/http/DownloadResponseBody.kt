package com.haolin.swift.downloader.library.http

import com.haolin.swift.downloader.library.core.controller.DownloadController
import com.haolin.swift.downloader.library.core.listener.IDownloadListener
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.*

class DownloadResponseBody(
    private val responseBody: ResponseBody,
    val listener: IDownloadListener,
    val downloadController: DownloadController
) :
    ResponseBody() {
    private val bufferedSource: BufferedSource by lazy { source(responseBody.source()).buffer() }

    override fun contentLength(): Long = responseBody.contentLength()

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun source(): BufferedSource = bufferedSource

    private var downloadBytesRead = 0L

    private fun source(source: Source): Source {
        downloadBytesRead = 0L
        return object : ForwardingSource(source) {
            override fun read(sink: Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)

                if (downloadController.isPause()) {
                    listener.onStop(downloadBytesRead, contentLength())
                    return -1
                }
                //进度监听
                downloadBytesRead += if (bytesRead != -1L) bytesRead else 0
                listener.onProgressChange(downloadBytesRead, contentLength())
//                if (downloadBytesRead == contentLength()) {
//                    listener.onFinish(downloadBytesRead, contentLength())
//                }


//                val old = downloadBytesRead * 100 / contentLength()
//                downloadBytesRead += if (bytesRead != -1L) bytesRead else 0
//                val newV = downloadBytesRead * 100 / contentLength()
//                if (old != newV) {
//                    listener.onProgressChange(newV)
//                    if (downloadBytesRead >= contentLength()) {
//                        listener.onFinish(downloadBytesRead, contentLength())
//                    }
//                }
                return bytesRead
            }
        }
    }
}