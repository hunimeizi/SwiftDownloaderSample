package com.haolin.swift.downloader.library.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "TaskInfo")
data class TaskInfo(
    @PrimaryKey
    var id: Long,
    var fileName: String,
    var filePath: String,
    var url: String,
    var downloadedBytes: Long,
    var totalBytes: Long,
    var status: Int
) {
    fun getAbsolutePath() = "$filePath/$fileName"
}

const val TASK_STATUS_UNINITIALIZED = 0
const val TASK_STATUS_UNFINISHED = 1
const val TASK_STATUS_FINISH = 2