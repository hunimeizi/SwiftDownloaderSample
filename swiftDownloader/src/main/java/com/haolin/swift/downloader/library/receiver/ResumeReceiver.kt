package com.haolin.swift.downloader.library.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.haolin.swift.downloader.library.core.SwiftDownloader

class ResumeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        SwiftDownloader.resume()
    }
}
