package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DownloadStatus
import com.example.ui.theme.*
import com.example.ui.viewmodels.SniffeViewModel

@Composable
fun DashboardScreen(viewModel: SniffeViewModel) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val storage by viewModel.storageUsage.collectAsStateWithLifecycle()

    val totalBytes = storage ?: 0L
    val completedTasks = tasks.count { it.status == DownloadStatus.COMPLETED }
    val activeTasks = tasks.count { it.status == DownloadStatus.DOWNLOADING || it.status == DownloadStatus.SYNCING }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Analytics Dashboard",
            color = SleekPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            MetricCard(
                title = "Total Storage",
                value = formatBytes(totalBytes),
                icon = Icons.Default.Storage,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Saved to Drive",
                value = "$completedTasks files",
                icon = Icons.Default.CloudDone,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Network Activity",
            color = SleekTextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Card(
            colors = CardDefaults.cardColors(containerColor = SleekCard),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Active Downloads: $activeTasks", color = SleekTextSecondary)
                Text("Syncing to Cloud: ${tasks.count { it.status == DownloadStatus.SYNCING }}", color = SleekTextSecondary)
                Text("Pending in Queue: ${tasks.count { it.status == DownloadStatus.QUEUED }}", color = SleekTextSecondary)
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekCard),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = title, tint = SleekPrimary)
            Text(title, color = SleekTextSecondary, fontSize = 14.sp)
            Text(value, color = SleekTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (Math.log(bytes.toDouble()) / Math.log(1024.0)).toInt()
    val pre = "KMGTPE"[exp - 1]
    return String.format("%.1f %sB", bytes / Math.pow(1024.0, exp.toDouble()), pre)
}
