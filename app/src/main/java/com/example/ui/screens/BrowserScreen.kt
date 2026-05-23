package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.StorageType
import com.example.ui.theme.*
import com.example.ui.viewmodels.SniffeViewModel
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(viewModel: SniffeViewModel) {
    val context = LocalContext.current
    var urlInput by remember { mutableStateOf(viewModel.lastBrowserUrl) }
    var webView: WebView? by remember { mutableStateOf(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showDownloadDialog by remember { mutableStateOf(false) }
    var downloadUrl by remember { mutableStateOf("") }
    var downloadFileName by remember { mutableStateOf("") }
    
    val bookmarks by viewModel.bookmarks.collectAsState()
    val isBookmarked = bookmarks.contains(viewModel.lastBrowserUrl)
    
    var selectedStorage by remember { mutableStateOf(StorageType.LOCAL_PHONE) }
    var selectedQuality by remember { mutableStateOf("1080p") }

    var sniffedVideoUrl by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        onDispose {
            webView?.destroy()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SleekBackground)
    ) {
        // Address Bar Area
        Surface(
            color = SleekCard,
            contentColor = SleekTextPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SleekCardLight,
                        unfocusedContainerColor = SleekCardLight,
                        focusedBorderColor = SleekPrimary,
                        unfocusedBorderColor = SleekBorder,
                        focusedTextColor = SleekTextPrimary,
                        unfocusedTextColor = SleekTextPrimary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            var targetUrl = urlInput
                            if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                                targetUrl = "https://$targetUrl"
                            }
                            viewModel.updateBrowserUrl(targetUrl)
                            sniffedVideoUrl = null // Reset sniffed url on new page
                            webView?.loadUrl(targetUrl)
                        }
                    ),
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = SleekTextSecondary)
                    },
                    trailingIcon = {
                        Row {
                            if (urlInput.isNotEmpty()) {
                                IconButton(onClick = { urlInput = "" }, modifier = Modifier.size(36.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = SleekTextSecondary)
                                }
                            }
                            IconButton(onClick = { viewModel.toggleBookmark(viewModel.lastBrowserUrl) }, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    if (isBookmarked) Icons.Default.Star else Icons.Default.StarBorder, 
                                    contentDescription = "Bookmark", 
                                    tint = if (isBookmarked) SleekPrimary else SleekTextSecondary
                                )
                            }
                            IconButton(onClick = { sniffedVideoUrl = null; webView?.reload() }, modifier = Modifier.size(36.dp)) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SleekTextSecondary)
                            }
                        }
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        IconButton(
                            onClick = { sniffedVideoUrl = null; webView?.goBack() },
                            enabled = canGoBack
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = if (canGoBack) SleekPrimary else SleekTextSecondary.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(
                            onClick = { sniffedVideoUrl = null; webView?.goForward() },
                            enabled = canGoForward
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Forward",
                                tint = if (canGoForward) SleekPrimary else SleekTextSecondary.copy(alpha = 0.5f)
                            )
                        }
                    }
                    
                    Button(
                        onClick = { 
                            if (sniffedVideoUrl != null) {
                                downloadUrl = sniffedVideoUrl!!
                                downloadFileName = "video_${System.currentTimeMillis()}.mp4"
                                showDownloadDialog = true
                            } else {
                                android.widget.Toast.makeText(context, "Mencari video di halaman ini... Coba putar videonya agar terdeteksi.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (sniffedVideoUrl != null) SleekPrimary else SleekCardLight),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Sniff", modifier = Modifier.size(18.dp), tint = if (sniffedVideoUrl != null) SleekBackground else SleekTextSecondary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (sniffedVideoUrl != null) "Video Ditemukan! (Sniff)" else "Cari Video", color = if (sniffedVideoUrl != null) SleekBackground else SleekTextSecondary)
                    }
                }
            }
        }

        // WebView Area
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { androidViewContext ->
                    WebView(androidViewContext).apply {
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.mediaPlaybackRequiresUserGesture = false
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                view?.let {
                                    it.url?.let { newUrl -> 
                                        urlInput = newUrl
                                        viewModel.updateBrowserUrl(newUrl)
                                    }
                                    canGoBack = it.canGoBack()
                                    canGoForward = it.canGoForward()
                                }
                            }

                            override fun onLoadResource(view: WebView?, url: String?) {
                                super.onLoadResource(view, url)
                                url?.let {
                                    if (it.contains(".mp4") || it.contains(".mkv") || it.contains(".webm") || it.contains(".m3u8")) {
                                        if (sniffedVideoUrl == null && !it.contains("google_ads") && !it.contains("youtube.com/api")) {
                                            sniffedVideoUrl = it
                                        }
                                    }
                                }
                            }
                            
                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): android.webkit.WebResourceResponse? {
                                request?.url?.toString()?.let { url ->
                                    if (url.contains(".mp4") || url.contains(".mkv") || url.contains(".webm") || url.contains(".m3u8")) {
                                        if (sniffedVideoUrl == null && !url.contains("google_ads") && !url.contains("youtube.com/api")) {
                                            sniffedVideoUrl = url
                                        }
                                    }
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                        }
                        loadUrl(viewModel.lastBrowserUrl)
                        webView = this
                    }
                },
                update = { view ->
                    webView = view
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    if (showDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showDownloadDialog = false },
            containerColor = SleekCard,
            title = { Text("Sniffed Video Found", color = SleekTextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("URL: $downloadUrl", color = SleekTextSecondary, style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    OutlinedTextField(
                        value = downloadFileName,
                        onValueChange = { downloadFileName = it },
                        label = { Text("Save As", color = SleekTextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SleekTextPrimary,
                            unfocusedTextColor = SleekTextPrimary,
                            focusedBorderColor = SleekPrimary
                        )
                    )
                    
                    Text("Storage Location", color = SleekTextPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    Row {
                        StorageChoice(text = "Local", selected = selectedStorage == StorageType.LOCAL_PHONE) { selectedStorage = StorageType.LOCAL_PHONE }
                        Spacer(Modifier.width(8.dp))
                        StorageChoice(text = "Google Drive", selected = selectedStorage == StorageType.G_DRIVE) { selectedStorage = StorageType.G_DRIVE }
                    }
                    
                    Text("Quality (If available)", color = SleekTextPrimary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Auto", "1080p", "720p", "480p").forEach { q ->
                            QualityChoice(text = q, selected = selectedQuality == q) { selectedQuality = q }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (downloadUrl.isNotBlank() && downloadFileName.isNotBlank()) {
                        viewModel.addTask(downloadUrl, downloadFileName, 1, selectedStorage, selectedQuality)
                        showDownloadDialog = false
                    }
                }) {
                    Text("Add to Queue", color = SleekPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDownloadDialog = false }) {
                    Text("Cancel", color = SleekTextSecondary)
                }
            }
        )
    }
}

@Composable
fun StorageChoice(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SleekPrimary,
            selectedLabelColor = SleekBackground,
            labelColor = SleekTextSecondary
        )
    )
}

@Composable
fun QualityChoice(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SleekPrimary,
            selectedLabelColor = SleekBackground,
            labelColor = SleekTextSecondary
        )
    )
}
