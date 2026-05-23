package com.example.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.QueueScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekTextSecondary
import com.example.ui.viewmodels.SniffeViewModel

import com.example.ui.screens.PlayerScreen
import android.util.Base64

@Composable
fun SniffeApp(viewModel: SniffeViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route

    val showBottomBar = currentDestination != null && !currentDestination.startsWith("player")

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = com.example.ui.theme.SleekCard
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                        label = { Text("Overview") },
                        selected = currentDestination == "dashboard",
                        onClick = { navController.navigate("dashboard") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekPrimary,
                            unselectedIconColor = SleekTextSecondary,
                            unselectedTextColor = SleekTextSecondary,
                            indicatorColor = com.example.ui.theme.SleekIconBg
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.List, contentDescription = "Queue") },
                        label = { Text("Queue") },
                        selected = currentDestination == "queue",
                        onClick = { navController.navigate("queue") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekPrimary,
                            unselectedIconColor = SleekTextSecondary,
                            unselectedTextColor = SleekTextSecondary,
                            indicatorColor = com.example.ui.theme.SleekIconBg
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Public, contentDescription = "Browser") },
                        label = { Text("Browser") },
                        selected = currentDestination == "browser",
                        onClick = { navController.navigate("browser") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekPrimary,
                            unselectedIconColor = SleekTextSecondary,
                            unselectedTextColor = SleekTextSecondary,
                            indicatorColor = com.example.ui.theme.SleekIconBg
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentDestination == "settings",
                        onClick = { navController.navigate("settings") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SleekPrimary,
                            selectedTextColor = SleekPrimary,
                            unselectedIconColor = SleekTextSecondary,
                            unselectedTextColor = SleekTextSecondary,
                            indicatorColor = com.example.ui.theme.SleekIconBg
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("dashboard") { DashboardScreen(viewModel) }
            composable("queue") { 
                QueueScreen(viewModel, onPlay = { url, title ->
                    val safeUrl = url.ifBlank { "none" }
                    val safeTitle = title.ifBlank { "none" }
                    val encodedUrl = Base64.encodeToString(safeUrl.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
                    val encodedTitle = Base64.encodeToString(safeTitle.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
                    navController.navigate("player/$encodedUrl/$encodedTitle")
                })
            }
            composable("browser") { BrowserScreen(viewModel) }
            composable("settings") { SettingsScreen(viewModel) }
            composable("player/{url}/{title}") { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""
                
                var url = "none"
                var title = "none"
                try {
                    url = String(Base64.decode(encodedUrl, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
                    title = String(Base64.decode(encodedTitle, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                
                PlayerScreen(videoUrl = url, title = title, onNavigateUp = { navController.navigateUp() })
            }
        }
    }
}
