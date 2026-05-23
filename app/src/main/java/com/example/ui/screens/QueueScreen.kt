package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DownloadStatus
import com.example.data.DownloadTask
import com.example.data.StorageType
import com.example.ui.theme.*
import com.example.ui.viewmodels.SniffeViewModel

@Composable
fun QueueScreen(viewModel: SniffeViewModel, onPlay: (String, String) -> Unit = { _, _ -> }) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = SleekPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Download", tint = SleekBackground)
            }
        },
        containerColor = SleekBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Download Queue",
                color = SleekPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 24.dp)
            )

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No active downloads", color = SleekTextSecondary)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(tasks, key = { it.id }) { task ->
                        DownloadTaskItem(
                            task = task,
                            onUpdatePriority = { newPrio -> viewModel.updatePriority(task, newPrio) },
                            onDelete = { viewModel.deleteTask(task.id) },
                            onPlay = { 
                                val playUrl = if (task.status == DownloadStatus.COMPLETED && task.localFilePath != null) {
                                    android.net.Uri.fromFile(java.io.File(task.localFilePath)).toString()
                                } else {
                                    task.url
                                }
                                onPlay(playUrl, task.fileName) 
                            },
                            onRename = { newName -> viewModel.renameTask(task, newName) },
                            onExportToLocal = { viewModel.updateStorageType(task, StorageType.LOCAL_PHONE) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var url by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SleekCard,
            title = { Text("New Download", color = SleekTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Video URL", color = SleekTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary,
                            focusedBorderColor = SleekPrimary
                        )
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("File Name", color = SleekTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary,
                            focusedBorderColor = SleekPrimary
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (url.isNotBlank() && name.isNotBlank()) {
                        viewModel.addTask(url, name, 1)
                        showAddDialog = false
                    }
                }) {
                    Text("Start Download", color = SleekPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            }
        )
    }
}

@Composable
fun DownloadTaskItem(
    task: DownloadTask, 
    onUpdatePriority: (Int) -> Unit, 
    onDelete: () -> Unit, 
    onPlay: () -> Unit,
    onRename: (String) -> Unit,
    onExportToLocal: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    
    val progress = if (task.totalBytes > 0) task.downloadedBytes.toFloat() / task.totalBytes else 0f
    
    if (showRenameDialog) {
        var newName by remember { mutableStateOf(task.fileName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = SleekCard,
            title = { Text("Rename File", color = SleekTextPrimary) },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary,
                        focusedBorderColor = SleekPrimary
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { onRename(newName); showRenameDialog = false }) {
                    Text("Rename", color = SleekPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            }
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = SleekCardLight),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.fileName,
                    color = SleekTextPrimary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Row {
                    IconButton(onClick = { onUpdatePriority(task.priority + 1) }) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Increase Priority", tint = SleekPrimary)
                    }
                    IconButton(onClick = { onUpdatePriority(task.priority - 1) }) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Decrease Priority", tint = SleekPrimary)
                    }
                }
            }
            
            Text(
                text = "Status: ${task.status.name} | Prio: ${task.priority} | ${task.storageType.name}",
                color = SleekTextSecondary,
                fontSize = 10.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val barColor = when (task.status) {
                DownloadStatus.SYNCING -> SleekPrimaryDark
                DownloadStatus.COMPLETED -> SleekSuccess
                DownloadStatus.FAILED -> SleekError
                else -> SleekPrimary
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = barColor,
                trackColor = SleekBorder
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatBytes(task.downloadedBytes)} / ${formatBytes(task.totalBytes)}",
                    color = SleekTextSecondary,
                    fontSize = 10.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onPlay, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = SleekPrimary, modifier = Modifier.size(20.dp))
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More", tint = SleekTextSecondary, modifier = Modifier.size(20.dp))
                        }
                        DropdownMenu(
                            expanded = showMenu, 
                            onDismissRequest = { showMenu = false },
                            containerColor = SleekCardLight
                        ) {
                            DropdownMenuItem(text = { Text("Rename", color = SleekTextPrimary) }, onClick = { showMenu = false; showRenameDialog = true })
                            if (task.storageType == StorageType.G_DRIVE) {
                                DropdownMenuItem(text = { Text("Export to Phone", color = SleekTextPrimary) }, onClick = { showMenu = false; onExportToLocal() })
                            }
                            DropdownMenuItem(text = { Text("Delete", color = SleekError) }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }
            }
        }
    }
}
