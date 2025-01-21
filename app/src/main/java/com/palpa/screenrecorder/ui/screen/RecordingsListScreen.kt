package com.palpa.screenrecorder.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.palpa.screenrecorder.viewmodel.Recording
import com.palpa.screenrecorder.viewmodel.RecordingsViewModel
import java.text.SimpleDateFormat
import java.util.*


@Composable
fun RecordingsListScreen(recordingsViewModel: RecordingsViewModel) {
    val recordings by recordingsViewModel.recordings.collectAsStateWithLifecycle()

    var showRenameDialog by remember { mutableStateOf(false) }
    var recordingToRename by remember { mutableStateOf<Recording?>(null) }
    var newName by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var recordingToDelete by remember { mutableStateOf<Recording?>(null) }

    // ---------- RENAME DIALOG ----------
    if (showRenameDialog && recordingToRename != null) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Recording") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { typed ->
                        if (typed.length <= 12) {
                            newName = typed
                        }
                    },
                    label = { Text("New Name (max 12 chars)") }
                )
            },
            confirmButton = {
                Button(onClick = {
                    val r = recordingToRename ?: return@Button
                    if (newName.isNotBlank()) {
                        val finalName = "${newName}.mp4"
                        recordingsViewModel.renameRecording(r, finalName)
                    }
                    showRenameDialog = false
                    newName = ""
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    newName = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---------- DELETE CONFIRMATION DIALOG ----------
    if (showDeleteDialog && recordingToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Recording") },
            text = {
                Text("Are you sure you want to delete \"${recordingToDelete?.displayName}\"?")
            },
            confirmButton = {
                Button(onClick = {
                    val recToDel = recordingToDelete ?: return@Button
                    recordingsViewModel.deleteRecording(recToDel)
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ---------- MAIN CONTENT ----------
    if (recordings.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No recordings found", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(recordings) { rec ->
                RecordingCard(
                    recording = rec,
                    onPlay = { recordingsViewModel.playRecording(rec) },
                    onRename = {
                        // Show the original base name (no auto "video_")
                        val originalBase = rec.displayName.removeSuffix(".mp4")
                        newName = originalBase.take(12)
                        recordingToRename = rec
                        showRenameDialog = true
                    },
                    onDelete = {
                        recordingToDelete = rec
                        showDeleteDialog = true
                    },
                    onShare = {
                        recordingsViewModel.shareRecording(rec)
                    }
                )
            }
        }
    }
}

@Composable
fun RecordingCard(
    recording: Recording,
    onPlay: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPlay() },
    ) {
        val dateString = remember(recording.dateAdded) {
            val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            sdf.format(Date(recording.dateAdded * 1000L))
        }

        val baseName = recording.displayName.removeSuffix(".mp4")

        val displayName: String = if (baseName.startsWith("video_")) {
            // It's the default auto file => Show "video_" + last 5
            val lastFive = baseName.removePrefix("video_").takeLast(5)
            "video_${lastFive}.mp4"
        } else {
            // It's user renamed => Show as is
            recording.displayName
        }

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${recording.size / 1024} KB",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(0.5.dp)) {
                IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, contentDescription = "Share")
                }
                IconButton(onClick = onRename) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}
