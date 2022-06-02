# SwiftDownloader

#### 介绍
 **_SwiftDownloader 是基于OkHttp和kotlin协程实现的下载器，它能在后台进行下载任务并轻松地让您在下载文件时获取进度，它能随时停止、恢复、取消任务，还可以方便地查询下载的任务和已完成的任务的信息。_** 

#### 功能&特性


 :star: **下载文件**

 :star: **监听下载**

 :star: **断点续传**

 :star: **随时控制下载**

 :star: **查询下载任务 (支持返回LiveData)**

 :star: **可通过通知栏显示下载情况** 

 :star: **下载多媒体文件加入多媒体库** 

 :star: **自动/手动清除缓存文件** 

 :star: **支持链式调用**


#### 导入依赖

1. 把它添加到你的根目录build.gradle中，在repositories的最后:
```groovy

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

```

2. 添加依赖：
![version](https://jitpack.io/v/com.gitee.jiang_li_jie_j/awesome-downloader.svg)
```groovy

repositories {
  google()
  mavenCentral()
}

dependencies {
  implementation 'io.github.hunimeizi:haolinSwiftDownloader:1.0.3'
}

```


#### 使用说明

1.申请读写权限，网络权限

2.初始化下载器，传入Application的context

kotlin：
```kotlin
	 SwiftDownloader.initWithServiceMode(application)
```
java：
```java    
	 SwiftDownloader.INSTANCE.initWithServiceMode(getApplicationContext());
```
3.下载文件 

kotlin:
 ```kotlin
	 val url = "https://storage.jd.com/jdmobile/JDMALL-PC2.apk"
        //获取应用外部照片储存路径
        val filePath = PathSelector(applicationContext).getPicturesDirPath()
        val fileName = "jd.apk"
        //加入下载队列
        SwiftDownloader.enqueue(url, filePath, fileName)
```
java：
```java
        String url="https://storage.jd.com/jdmobile/JDMALL-PC2.apk";
        //获取应用外部照片储存路径
        String filePath = new PathSelector(getApplicationContext()).getPicturesDirPath();
        String fileName = "jd.apk";
        //加入下载队列
        SwiftDownloader.INSTANCE.enqueue(url,filePath,fileName);

```
4.下载控制

kotlin:
```kotlin
        //停止全部
        SwiftDownloader.stopAll()
        //恢复下载
        SwiftDownloader.resumeAndStart()
        //取消当前
        SwiftDownloader.cancel()
        //取消全部
        SwiftDownloader.cancelAll()
```

5.设置监听

kotlin:
```kotlin
        SwiftDownloader.setOnProgressChange { progress ->
            //do something...
        }.setOnStop { downloadBytes, totalBytes ->
            //do something...
        }.setOnFinished { filePath, fileName ->
            //do something...
        }.setOnError { exception ->
            //do something...
        }
```

6.设置自定义通知栏

设置中确保showNotification为true
```kotlin
 SwiftDownloader.option.showNotification = true
```
调用setNotificationSender()

override 抽象类NotificationSender 的三个方法

kotlin:
```kotlin
 val remoteViews = RemoteViews(packageName, R.layout.notify_download) // Android12 展开布局
val remoteViewsCustom = RemoteViews(packageName, R.layout.notify_download_custom) // Android12收起后通知的布局
SwiftDownloader.notificationSender = object : NotificationSender(applicationContext) {
    //创建显示任务下载进度的Notification
    override fun buildDownloadProgressNotification(
        progress: Int,
        fileName: String
    ): Notification {
        remoteViews.setProgressBar(R.id.pb_progress, 100, progress, false)
        remoteViews.setTextViewText(R.id.tv_progress, "已下载$progress%")
        remoteViewsCustom.setTextViewText(R.id.tv_progress, "已下载$progress%")
        return NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContent(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViewsCustom)
            .setCustomBigContentView(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    //创建显示任务下载停止的Notification
    override fun buildDownloadStopNotification(fileName: String): Notification {
        remoteViewsCustom.setTextViewText(R.id.tv_progress, "下载停止")
        return NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContent(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(remoteViewsCustom)
            .setCustomBigContentView(remoteViews)
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
        remoteViewsCustom.setTextViewText(R.id.tv_progress, "下载完成")
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
            val pendingIntent = PendingIntent.getActivities(
                this@MainActivity, 0,
                arrayOf(intent),  if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT else PendingIntent.FLAG_UPDATE_CURRENT
            )
            NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(remoteViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViewsCustom)
                .setCustomBigContentView(remoteViews)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        } else {
            NotificationCompat.Builder(context, DOWNLOAD_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContent(remoteViews)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(remoteViewsCustom)
                .setCustomBigContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }
}
```

_(通过setNotificationSender()设置的通知栏)_

#### 注意需要打开通知权限，部分手机默认通知权限是关闭的

#### 内嵌上传 Maven Central
详细请看教程
[JCenter已经提桶跑路，是时候学会上传到Maven Central了](https://mp.weixin.qq.com/s/CrfYc1KsugJKPy_0rDZ49Q)
