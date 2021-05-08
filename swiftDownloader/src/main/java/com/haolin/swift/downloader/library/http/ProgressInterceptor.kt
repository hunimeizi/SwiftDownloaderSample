package com.haolin.swift.downloader.library.http


import com.haolin.swift.downloader.library.core.controller.DownloadController
import com.haolin.swift.downloader.library.core.listener.IDownloadListener
import okhttp3.Interceptor
import okhttp3.Response


class ProgressInterceptor(
    private val listener: IDownloadListener,
    private val downloadController: DownloadController
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse: Response = chain.proceed(chain.request())
        return originalResponse.newBuilder()
            .body(
                DownloadResponseBody(
                    originalResponse.body()!!,
                    listener,
                    downloadController
                )
            )
            .build()
    }
}
