package com.haolin.swift.downloader.library.core.sender

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.haolin.swift.downloader.library.R

const val CHANNEL_NAME = "download"
const val DOWNLOAD_CHANNEL_ID = "NotificationSender"
const val NOTIFICATION_PROGRESS_MAX = 100
const val NOTIFICATION_DOWNLOAD_ID = 2234
const val NOTIFICATION_DONE_ID = 2277

abstract class NotificationSender(protected val context: Context) {
    val descriptionText = context.getString(R.string.notification_description)
    abstract fun buildDownloadProgressNotification(progress: Int, fileName: String): Notification

    abstract fun buildDownloadStopNotification(fileName: String): Notification

    abstract fun buildDownloadDoneNotification(filePath: String, fileName: String): Notification

    /**
     * 创建NotificationChannel，但仅在API 26+上创建，因为NotificationChannel类是新的，并且不在支持库中.
     * 由于您必须先创建通知渠道，然后才能在 Android 8.0 及更高版本上发布任何通知，因此应在应用启动时立即执行这段代码。
     * 反复调用这段代码是安全的，因为创建现有通知渠道不会执行任何操作。
     */
    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(DOWNLOAD_CHANNEL_ID, CHANNEL_NAME, importance)
            // 配置通知渠道的属性
            mChannel.description = descriptionText
            // 设置通知出现时的闪灯（如果 android 设备支持的话）
            mChannel.enableLights(true)
            mChannel.lightColor = Color.RED
            // 设置通知出现时的震动（如果 android 设备支持的话）
            mChannel.enableVibration(true)
            mChannel.vibrationPattern = longArrayOf(100, 200, 300)
            //最后在notificationmanager中创建该通知渠道
            mNotificationManager.createNotificationChannel(mChannel)
        }
    }


    fun cancelDownloadProgressNotification() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_DOWNLOAD_ID)
    }

    fun showDownloadStopNotification(fileName: String) {
        val notification = buildDownloadStopNotification(fileName)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_DOWNLOAD_ID, notification)
    }

    fun showDownloadDoneNotification(fileName: String, filePath: String) {
        val notification = buildDownloadDoneNotification(filePath, fileName)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_DONE_ID, notification)
    }

    fun showDownloadProgressNotification(progress: Int, fileName: String) {
        val notification = buildDownloadProgressNotification(progress, fileName)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_DOWNLOAD_ID, notification)
    }
}