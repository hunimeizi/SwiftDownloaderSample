package com.haolin.swift.downloader.library.core


class SwiftDownloaderOption {
    var timeout: Long = 300
    var showNotification = true
    var notifyMediaStoreWhenItDone = true

    //未实装
    var serviceModeAutoClose = false
    var autoCloseTime = 300_000
}