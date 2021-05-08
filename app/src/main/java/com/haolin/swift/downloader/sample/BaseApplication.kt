package com.haolin.swift.downloader.sample

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build

class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mNotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // 通知渠道的id
            val id = resources.getString(R.string.app_name)
            // 用户可以看到的通知渠道的名字.
            val name: CharSequence = "通知"
            // 用户可以看到的通知渠道的描述
            val description = "通知"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(id, name, importance)
            // 配置通知渠道的属性
            mChannel.description = description
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
}