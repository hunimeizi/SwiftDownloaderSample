package com.haolin.swift.downloader.library.database

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface TaskInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vararg taskInfo: TaskInfo)

    @Delete
    suspend fun delete(vararg taskInfo: TaskInfo): Int

    @Delete
    suspend fun deleteArray(taskInfos: Array<TaskInfo>): Int

    @Update
    suspend fun update(vararg taskInfo: TaskInfo)

    @Query("select * from TaskInfo where url = :url")
    suspend fun findByUrls(url:String): TaskInfo?

    @Query("select * from TaskInfo")
    suspend fun queryAll(): MutableList<TaskInfo>

    @Query("select * from TaskInfo")
    fun queryAllAndReturnLiveData(): LiveData<List<TaskInfo>>

    @Query("select * from TaskInfo where status < $TASK_STATUS_FINISH")
    suspend fun queryUnfinished(): MutableList<TaskInfo>

    @Query("select * from TaskInfo where status < $TASK_STATUS_FINISH")
    fun queryUnfinishedLiveData(): LiveData<MutableList<TaskInfo>>

    @Query("select * from TaskInfo where status = $TASK_STATUS_FINISH")
    suspend fun queryFinished(): MutableList<TaskInfo>

    @Query("select * from TaskInfo where status = $TASK_STATUS_FINISH")
    fun queryFinishedLiveData(): LiveData<MutableList<TaskInfo>>

    @Query("delete from TaskInfo where id=:taskInfoID")
    suspend fun deleteByID(taskInfoID: Long)

    @Query("delete from TaskInfo where url=:url")
    suspend fun deleteByUrl(url:String)

    @Query("delete from TaskInfo where status between $TASK_STATUS_UNINITIALIZED and $TASK_STATUS_UNFINISHED")
    suspend fun deleteAllUnfinishedTaskInfo()

    @Query("delete from TaskInfo")
    suspend fun deleteAllTaskInfo()
}