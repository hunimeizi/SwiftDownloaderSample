package com.haolin.swift.downloader.sample

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.haolin.swift.downloader.library.core.SwiftDownloader
import com.haolin.swift.downloader.library.core.sender.NotificationSender
import com.haolin.swift.downloader.library.tool.PathSelector
import com.haolin.swift.downloader.library.tool.isApkFile
import java.io.File


class MainActivity : AppCompatActivity() {

    private val progressCircular by lazy { findViewById<ProgressBar>(R.id.progress_circular) }
    private val tvProgress by lazy { findViewById<TextView>(R.id.tvProgress) }
    private var isDownSuccess = false
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
                return if (isApkFile(fileName)) {
                    val file = File("$filePath/$fileName")
                    val uri = Uri.fromFile(file)
                    val intent = Intent(Intent.ACTION_VIEW)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        val contentUri = FileProvider.getUriForFile(
                            this@MainActivity, BuildConfig.APPLICATION_ID + ".fileprovider", file
                        )
                        intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
                    } else {
                        intent.setDataAndType(uri, "application/vnd.android.package-archive")
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val pendingIntent = PendingIntent.getActivities(this@MainActivity,0,
                        arrayOf(intent),PendingIntent.FLAG_UPDATE_CURRENT)
                    NotificationCompat.Builder(context, resources.getString(R.string.app_name))
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContent(remoteViews)
                        .setContentIntent(pendingIntent)
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

    private fun gotoDown() {
        val url = "https://storage.jd.com/jdmobile/JDMALL-PC2.apk"
        //获取应用外部照片储存路径
        val filePath = PathSelector(applicationContext).getDownloadsDirPath()
        val fileName = "jd.apk"
        //加入下载队列
        SwiftDownloader.enqueue(true, url, filePath, fileName)
        SwiftDownloader.setOnProgressChange { progress ->
            progressCircular.progress = progress.toInt()
            tvProgress.text = "已下载$progress%"
            //do something...
        }.setOnStop { downloadBytes, totalBytes ->
            Toast.makeText(this, "下载暂停", Toast.LENGTH_SHORT).show()
            //do something...
        }.setOnFinished { filePath, fileName ->
            SwiftDownloader.deleteByUrl(url)
            Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show()
            progressCircular.progress = 100
            tvProgress.text = "下载完成"
            Log.e("lyb========下载路径==", "$filePath/$fileName")
            installUseAS(this, "$filePath/$fileName")
            //do something...
        }.setOnError { exception ->
            SwiftDownloader.cancelAll()
            //do something...
            Toast.makeText(this, "下载错误", Toast.LENGTH_SHORT).show()
        }
    }

    private fun installUseAS(mContext: Context, filePath: String) {
        val file = File(filePath)
        val uri = Uri.fromFile(file)
        val intent = Intent(Intent.ACTION_VIEW)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val contentUri = FileProvider.getUriForFile(
                mContext, BuildConfig.APPLICATION_ID + ".fileprovider", file
            )
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        mContext.startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isDownSuccess)
            SwiftDownloader.cancelAll()
    }
}