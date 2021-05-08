package com.haolin.swift.downloader.library.database

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Room
import com.haolin.swift.downloader.library.tool.TAG


const val DATABASE_NAME = "SwiftDownloader_DB"

class DownloadTaskManager(private val appContext: Context) {

    private val database by lazy {
        Room.databaseBuilder(appContext, DownloaderRoomDatabase::class.java, DATABASE_NAME).build()
    }

    private val dao by lazy { database.getTaskInfoDao() }

    suspend fun getAllTaskInfo(): MutableList<TaskInfo> = dao.queryAll()
    suspend fun getUnfinishedTaskInfo(): MutableList<TaskInfo> = dao.queryUnfinished()

    fun getAllTaskInfoLiveData(): LiveData<List<TaskInfo>> = dao.queryAllAndReturnLiveData()
    fun getUnfinishedTaskInfoLiveData(): LiveData<MutableList<TaskInfo>> =
        dao.queryUnfinishedLiveData()

    suspend fun getFinishedTaskInfo(): MutableList<TaskInfo> = dao.queryFinished()
    fun getFinishedTaskInfoLiveData(): LiveData<MutableList<TaskInfo>> =
        dao.queryFinishedLiveData()

    suspend fun insertTaskInfo(taskInfo: TaskInfo) {
        dao.insert(taskInfo)
    }

    suspend fun deleteTaskInfo(taskInfo: TaskInfo): Int {
        Log.d(TAG, "deleteTaskInfo $taskInfo")
        return dao.delete()
    }

    suspend fun deleteTaskInfoArray(taskInfoArray: Array<TaskInfo>): Int {
        Log.d(TAG, "deleteTaskInfo $taskInfoArray")
        return dao.deleteArray(taskInfoArray)
    }

    suspend fun updateTaskInfo(taskInfo: TaskInfo) {
        dao.update(taskInfo)
    }

    suspend fun deleteTaskInfoByID(id: Long) {
        dao.deleteByID(id)
    }

    suspend fun deleteAllUnfinishedTaskInfo() {
        dao.deleteAllUnfinishedTaskInfo()
    }
}