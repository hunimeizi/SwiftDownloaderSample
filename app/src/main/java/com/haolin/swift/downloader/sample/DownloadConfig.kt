package com.haolin.swift.downloader.sample

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import com.haolin.swift.downloader.library.core.SwiftDownloader
import com.haolin.swift.downloader.library.core.sender.DOWNLOAD_CHANNEL_ID
import com.haolin.swift.downloader.library.core.sender.NotificationSender
import com.haolin.swift.downloader.library.tool.isApkFile
import java.io.File

fun FragmentActivity.configuration() {
    SwiftDownloader.initWithServiceMode(application)
    SwiftDownloader.option.showNotification = true
    val remoteViews = RemoteViews(packageName, R.layout.notify_download)
    val remoteViewsCustom =
        RemoteViews(packageName, R.layout.notify_download_custom)
    SwiftDownloader.notificationSender = object : NotificationSender(application) {
        //创建显示任务下载进度的Notification
        override fun buildDownloadProgressNotification(
            progress: Int,
            fileName: String,
        ): Notification {
            remoteViews.setProgressBar(R.id.pb_progress, 100, progress, false)
            remoteViews.setTextViewText(R.id.tv_progress, "已下载$progress%")
            remoteViewsCustom.setTextViewText(R.id.tv_progress, "已下载$progress%")
            return NotificationCompat.Builder(this@configuration, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOnlyAlertOnce(true)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViewsCustom)
                .setCustomBigContentView(remoteViews)
                .build()
        }

        //创建显示任务下载停止的Notification
        override fun buildDownloadStopNotification(fileName: String): Notification {
            return NotificationCompat.Builder(this@configuration, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViewsCustom)
                .setCustomBigContentView(remoteViews)
                .build()
        }

        //创建显示任务下载完成的Notification
        override fun buildDownloadDoneNotification(
            filePath: String,
            fileName: String,
        ): Notification {
            remoteViews.setProgressBar(R.id.pb_progress, 100, 100, false)
            remoteViews.setTextViewText(R.id.tv_progress, "下载完成")
            remoteViewsCustom.setTextViewText(R.id.tv_progress, "下载完成")
            return if(isApkFile(fileName)) {
                val file = File("$filePath/$fileName")
                val uri = Uri.fromFile(file)
                val intent = Intent(Intent.ACTION_VIEW)
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val contentUri = FileProvider.getUriForFile(
                        this@configuration, "$packageName.fileprovider", file
                    )
                    intent.setDataAndType(
                        contentUri,
                        "application/vnd.android.package-archive"
                    )
                } else {
                    intent.setDataAndType(uri, "application/vnd.android.package-archive")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val pendingIntent = PendingIntent.getActivities(
                    this@configuration, 0,
                    arrayOf(intent),
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
                )
                NotificationCompat.Builder(this@configuration, DOWNLOAD_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViewsCustom)
                    .setCustomBigContentView(remoteViews)
                    .build()
            } else {
                NotificationCompat.Builder(this@configuration, DOWNLOAD_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                    .setCustomContentView(remoteViewsCustom)
                    .setCustomBigContentView(remoteViews)
                    .build()
            }
        }
    }
}
