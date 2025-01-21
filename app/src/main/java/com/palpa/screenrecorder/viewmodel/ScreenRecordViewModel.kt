package com.palpa.screenrecorder.viewmodel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.palpa.screenrecorder.config.ScreenRecordConfig
import com.palpa.screenrecorder.service.ScreenRecordService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class ScreenRecordViewModel : ViewModel() {


    val isRecording = ScreenRecordService.isServiceRunning
        .stateIn(viewModelScope, SharingStarted.Lazily, false)


    fun startRecording(context: Context, config: ScreenRecordConfig) {
        val serviceIntent = Intent(context, ScreenRecordService::class.java).apply {
            action = ScreenRecordService.START_RECORDING
            putExtra(ScreenRecordService.KEY_RECORDING_CONFIG, config)
        }
        context.startForegroundService(serviceIntent)
    }


    fun stopRecording(context: Context) {
        Intent(context, ScreenRecordService::class.java).also {
            it.action = ScreenRecordService.STOP_RECORDING
            context.startForegroundService(it)
        }
    }
}
