package com.haolin.swift.downloader.library.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.haolin.swift.downloader.library.core.SwiftDownloader

class CancelAllReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        SwiftDownloader.cancelAll()
    }
}
