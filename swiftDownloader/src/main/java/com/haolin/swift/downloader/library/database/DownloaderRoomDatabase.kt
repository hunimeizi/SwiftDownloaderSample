package com.haolin.swift.downloader.library.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TaskInfo::class], version = 1, exportSchema = false)
abstract class DownloaderRoomDatabase : RoomDatabase() {
    abstract fun getTaskInfoDao(): TaskInfoDao
}