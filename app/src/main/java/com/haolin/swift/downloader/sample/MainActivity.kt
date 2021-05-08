package com.haolin.swift.downloader.sample

import android.Manifest
import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.haolin.swift.downloader.library.core.SwiftDownloader
import com.haolin.swift.downloader.library.core.sender.NotificationSender
import com.haolin.swift.downloader.library.tool.PathSelector
import com.haolin.swift.downloader.library.tool.isImageFile


class MainActivity : AppCompatActivity() {

    private val progressCircular by lazy { findViewById<ProgressBar>(R.id.progress_circular) }
    private val tvProgress by lazy { findViewById<TextView>(R.id.tvProgress) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SwiftDownloader.initWithServiceMode(application)
        SwiftDownloader.option.showNotification = true
        val remoteViews = RemoteViews(packageName, R.layout.notify_download)
        SwiftDownloader.notificationSender = object : NotificationSender(applicationContext) {
            //创建显示任务下载进度的Notification
            override fun buildDownloadProgressNotification(
                progress: Int,
                fileName: String
            ): Notification {
                remoteViews.setProgressBar(R.id.pb_progress, 100, progress, false)
                remoteViews.setTextViewText(R.id.tv_progress, "已下载$progress%")
                return NotificationCompat.Builder(context, resources.getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            }

            //创建显示任务下载停止的Notification
            override fun buildDownloadStopNotification(fileName: String): Notification {
                return NotificationCompat.Builder(context, resources.getString(R.string.app_name))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContent(remoteViews)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()
            }

            //创建显示任务下载完成的Notification
            override fun buildDownloadDoneNotification(
                filePath: String,
                fileName: String
            ): Notification {
                remoteViews.setProgressBar(R.id.pb_progress, 100, 100, false)
                remoteViews.setTextViewText(R.id.tv_progress, "下载完成")
                return if (isImageFile(fileName)) {
                    val bitmap = BitmapFactory.decodeFile("$filePath/$fileName")
                    NotificationCompat.Builder(context, resources.getString(R.string.app_name))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContent(remoteViews)
                        .setStyle(
                            NotificationCompat.BigPictureStyle()
                                .bigPicture(bitmap)
                                .bigLargeIcon(null)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()

                } else {
                    NotificationCompat.Builder(context, resources.getString(R.string.app_name))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContent(remoteViews)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .build()
                }
            }
        }

        findViewById<Button>(R.id.btnDown).setOnClickListener {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1 && grantResults.size == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            gotoDown()
        } else {
            Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show()
        }
    }
    private fun gotoDown(){
        val url = "https://storage.jd.com/jdmobile/JDMALL-PC2.apk"
        //获取应用外部照片储存路径
        val filePath = PathSelector(applicationContext).getDownloadsDirPath()
        val fileName = "jd.apk"
        //加入下载队列
        SwiftDownloader.enqueue(url, filePath, fileName)
        SwiftDownloader.setOnProgressChange { progress ->
            progressCircular.progress = progress.toInt()
            tvProgress.text = "已下载$progress%"
            //do something...
        }.setOnStop { downloadBytes, totalBytes ->
            Toast.makeText(this, "下载暂停", Toast.LENGTH_SHORT).show()
            //do something...
        }.setOnFinished { filePath, fileName ->
            Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show()
            progressCircular.progress = 100
            tvProgress.text = "下载完成"
            SwiftDownloader.cancelAll()
            //do something...
        }.setOnError { exception ->
            //do something...
            Toast.makeText(this, "下载错误", Toast.LENGTH_SHORT).show()
        }
    }
}