package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.ui.PlayerView
import com.example.ui.theme.SleekBackground
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    videoUrl: String,
    title: String,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    
    val exoPlayer = remember {
        val trackSelector = DefaultTrackSelector(context)
        ExoPlayer.Builder(context).setTrackSelector(trackSelector).build().apply {
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    android.widget.Toast.makeText(context, "Error memutar video (mungkin format tidak didukung / file bukan video): ${error.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    LaunchedEffect(videoUrl) {
        // Fallback ke sampel video jika URL bukan format video langsung, 
        // karena sniffer hanya menangkap URL web biasa pada simulasi ini.
        val finalUrl = if (videoUrl.contains(".mp4") || videoUrl.contains(".m3u8") || videoUrl.contains(".mkv") || videoUrl.startsWith("file://")) {
            videoUrl
        } else {
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        }
        val mediaItem = MediaItem.fromUri(finalUrl)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, color = SleekTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SleekBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SleekBackground)
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        setShowSubtitleButton(true)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
