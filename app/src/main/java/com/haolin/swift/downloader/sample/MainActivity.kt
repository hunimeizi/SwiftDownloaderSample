package com.haolin.swift.downloader.sample

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.haolin.swift.downloader.library.core.SwiftDownloader
import com.haolin.swift.downloader.library.tool.PathSelector


class MainActivity : AppCompatActivity() {

    private val progressCircular by lazy { findViewById<ProgressBar>(R.id.progress_circular) }
    private val tvProgress by lazy { findViewById<TextView>(R.id.tvProgress) }
    private var isDownSuccess = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        configuration()
        findViewById<Button>(R.id.btnDown).setOnClickListener {
            val url = "https://storage.jd.com/jdmobile/JDMALL-PC2.apk"
            //获取应用外部照片储存路径
            val filePath = PathSelector(applicationContext).getDownloadsDirPath()
            val fileName = "jd.apk"
            //加入下载队列
            SwiftDownloader.enqueue(false, url, filePath, fileName)
        }

        findViewById<Button>(R.id.btnDown2).setOnClickListener {
            val url = "https://storage.jd.com/jdmobile/JDMALL-PC2.apk"
            //获取应用外部照片储存路径
            val filePath = PathSelector(applicationContext).getDownloadsDirPath()
            val fileName = "jd1.apk"
            SwiftDownloader.enqueue(false, url, filePath, fileName)
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1
        )
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
        SwiftDownloader.deleteAllTaskInfo()

        SwiftDownloader.setOnProgressChange { progress ->
            progressCircular.progress = progress.toInt()
            tvProgress.text = "已下载$progress%"
            //do something...
        }.setOnDownloadSpeed {
            Log.e("lyb======", "下载速度:$it")
        }
            .setOnStop { downloadBytes, totalBytes ->
                Toast.makeText(this, "下载暂停", Toast.LENGTH_SHORT).show()
                //do something...
            }.setOnFinished { filePath, fileName,totalBytes ->
//                SwiftDownloader.deleteByUrl(url)
                Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show()
                progressCircular.progress = 100
                tvProgress.text = "下载完成"
                Log.e("lyb========下载路径==", "$filePath/$fileName")
                Log.e("lyb========总大小==", "$totalBytes")
//                installUseAS(this, "$filePath/$fileName")
                //do something...
            }.setOnError { exception ->
                SwiftDownloader.cancelAll()
//                SwiftDownloader.deleteByUrl(url)
                //do something...
                Toast.makeText(this, "下载错误", Toast.LENGTH_SHORT).show()
            }
    }
    override fun onDestroy() {
        super.onDestroy()
        SwiftDownloader.cancelAll()
    }

}