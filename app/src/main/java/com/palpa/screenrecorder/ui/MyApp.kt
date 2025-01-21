package com.palpa.screenrecorder.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.palpa.screenrecorder.config.ScreenRecordConfig
import com.palpa.screenrecorder.ui.screen.RecordingsListScreen
import com.palpa.screenrecorder.ui.screen.ScreenRecordScreen
import com.palpa.screenrecorder.viewmodel.RecordingsViewModel
import com.palpa.screenrecorder.viewmodel.ScreenRecordViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp() {
    val context = LocalContext.current

    val screenRecordViewModel = viewModel<ScreenRecordViewModel>()
    val recordingsViewModel = viewModel<RecordingsViewModel>()

    val isRecording by screenRecordViewModel.isRecording.collectAsStateWithLifecycle()

    // For Android 13+ notification permission
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    val mediaProjectionManager = remember {
        context.getSystemService<MediaProjectionManager>()
    }

    val screenCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val config = ScreenRecordConfig(
            resultCode = result.resultCode,
            data = data
        )
        screenRecordViewModel.startRecording(context, config)
    }

   //POST_NOTIFICATIONS permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
        if (granted && !isRecording) {
            mediaProjectionManager?.let {
                screenCaptureLauncher.launch(it.createScreenCaptureIntent())
            }
        }
    }

    // Track which tab is selected
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            // A Column that stacks an App Bar with Tab Row
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text(text = "Screen Recorder")
                    },

                )
                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Screen Record") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            selectedTabIndex = 1
                            // Reload recordings when switching to second tab
                            recordingsViewModel.loadRecordings()
                        },
                        text = { Text("Recordings") }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> ScreenRecordScreen(
                    isRecording = isRecording,
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onStartRecording = {
                        mediaProjectionManager?.let {
                            screenCaptureLauncher.launch(it.createScreenCaptureIntent())
                        }
                    },
                    onStopRecording = {
                        screenRecordViewModel.stopRecording(context)
                    }
                )
                1 -> RecordingsListScreen(
                    recordingsViewModel = recordingsViewModel
                )
            }
        }
    }
}
