package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodels.SniffeViewModel

@Composable
fun SettingsScreen(viewModel: SniffeViewModel) {
    var isBackgroundEnabled by remember { mutableStateOf(true) }
    var syncEnabled by remember { mutableStateOf(false) }
    var gDriveLinked by remember { mutableStateOf(true) } // Changed since user enabled GDrive
    var saveToLocal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            color = SleekPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsSection(title = "Cloud Integration") {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Google Drive Account", color = SleekTextPrimary, fontSize = 16.sp)
                    Text(
                        text = if (gDriveLinked) "Connected to Drive" else "Not connected", 
                        color = SleekTextSecondary, 
                        fontSize = 12.sp
                    )
                }
                Button(
                    onClick = { gDriveLinked = !gDriveLinked },
                    colors = ButtonDefaults.buttonColors(containerColor = if (gDriveLinked) SleekBorder else SleekPrimary)
                ) {
                    Text(if (gDriveLinked) "Disconnect" else "Login", color = if (gDriveLinked) SleekTextPrimary else SleekBackground)
                }
            }
            SettingsToggle(
                title = "Save to Phone Memory",
                description = "Download videos to local storage instead of Google Drive",
                checked = saveToLocal,
                onCheckedChange = { saveToLocal = it }
            )
            SettingsToggle(
                title = "Cross-Device Sync",
                description = "Synchronize download queue across devices",
                checked = syncEnabled,
                onCheckedChange = { syncEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        
        SettingsSection(title = "Download Preferences") {
            SettingsToggle(
                title = "Background Downloading",
                description = "Continue downloading when app is closed",
                checked = isBackgroundEnabled,
                onCheckedChange = { isBackgroundEnabled = it }
            )
            
            Button(
                onClick = { /* Setup Schedule */ },
                colors = ButtonDefaults.buttonColors(containerColor = SleekCard),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text("Schedule Downloads", color = SleekPrimary)
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(
        text = title,
        color = SleekTextSecondary,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekCard),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = SleekTextPrimary, fontSize = 16.sp)
            Text(text = description, color = SleekTextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SleekBackground,
                checkedTrackColor = SleekPrimary,
                uncheckedThumbColor = SleekTextSecondary,
                uncheckedTrackColor = SleekBorder
            )
        )
    }
}
