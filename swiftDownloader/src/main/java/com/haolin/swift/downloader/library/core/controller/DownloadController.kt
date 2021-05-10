package com.haolin.swift.downloader.library.core.controller


class DownloadController {
    private var workState =
        WorkState.STOP
    @Synchronized
    fun pause() {
        workState = WorkState.STOP
    }

    @Synchronized
    fun start() {
        workState = WorkState.RUNNING
    }

    fun isPause() = workState == WorkState.STOP
}

enum class WorkState {
    RUNNING, STOP
}