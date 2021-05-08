package com.haolin.swift.downloader.library.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.haolin.swift.downloader.library.core.SwiftDownloader
import com.haolin.swift.downloader.library.tool.TAG

class StopReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: stop")
        SwiftDownloader.stopAll()
    }
}
