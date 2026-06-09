package com.example.ui.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ModifierParameter")
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    videoUrl: String,
    title: String,
    isLive: Boolean,
    modifier: Modifier = Modifier,
    backupUrl: String? = null,
    onFullScreenToggle: (Boolean) -> Unit = {},
    isFullScreenMode: Boolean = false
) {
    val context = LocalContext.current
    var activeUrl by remember(videoUrl) { mutableStateOf(videoUrl) }
    var currentBackupUrl by remember(backupUrl) { mutableStateOf(backupUrl) }

    // ExoPlayer state
    var isPlaying by remember { mutableStateOf(true) }
    var isBuffering by remember { mutableStateOf(true) }
    var playbackError by remember { mutableStateOf<String?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var volume by remember { mutableStateOf(1f) }
    var isMuted by remember { mutableStateOf(false) }

    // Progress updates for On-Demand Content
    var currentPosition by remember { mutableStateOf(0L) }
    var duration by remember { mutableStateOf(0L) }
    var bufferPosition by remember { mutableStateOf(0L) }

    // ExoPlayer creation lifecycle
    val exoPlayer = remember(activeUrl) {
        ExoPlayer.Builder(context).build().apply {
            playWhenReady = true
            val mediaItem = MediaItem.fromUri(activeUrl)
            setMediaItem(mediaItem)
            prepare()
        }
    }

    // Set volume helper
    LaunchedEffect(volume, isMuted) {
        exoPlayer.volume = if (isMuted) 0f else volume
    }

    // Listener for state updates
    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                isBuffering = state == Player.STATE_BUFFERING
                isPlaying = exoPlayer.isPlaying
                playbackError = null
                duration = exoPlayer.duration.coerceAtLeast(0L)
            }

            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlayerError(error: PlaybackException) {
                isBuffering = false
                playbackError = "Error de reproducción: " + (error.localizedMessage ?: "Señal temporalmente caída")
            }
        }
        exoPlayer.addListener(listener)

        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    // Track timeline progress for on-demand
    if (!isLive) {
        LaunchedEffect(exoPlayer, isPlaying) {
            while (isPlaying) {
                currentPosition = exoPlayer.currentPosition.coerceAtLeast(0L)
                bufferPosition = exoPlayer.bufferedPosition.coerceAtLeast(0L)
                delay(1000)
            }
        }
    }

    // Auto-hide controls timer
    LaunchedEffect(showControls) {
        if (showControls) {
            delay(5000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .testTag("video_player_container")
            .background(Color.Black)
            .height(if (isFullScreenMode) BoxWithConstraintsScope::maxHeight.toString().toIntOrNull()?.dp ?: 320.dp else 220.dp)
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                showControls = !showControls
            }
    ) {
        // HLS Player AndroidView integration
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Custom control layer in Compose
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            update = { playerView ->
                playerView.player = exoPlayer
            }
        )

        // Subtle gradient background behind controls
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(tween(300)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.6f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }

        // Top Status Overlay (Title, Signal tag / Fallback trigger)
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .align(Alignment.TopStart),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = if (isLive) Color.Red else MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = if (isLive) "EN VIVO" else "CINE LOCAL",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = if (isLive) "1080p HD" else "Bajo Demanda",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                // Fallback stream selector buttons for premium safety
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (currentBackupUrl != null) {
                        Button(
                            onClick = {
                                // Swap streaming source
                                val mainUrl = activeUrl
                                activeUrl = currentBackupUrl!!
                                currentBackupUrl = mainUrl
                                playbackError = null
                                isBuffering = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Sync,
                                contentDescription = "Intercambiar Señal",
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cambiar Señal", fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // Center Actions Layer (Play, Pause, Buffering Loader, Errors)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isBuffering) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            } else if (playbackError != null) {
                // Show fallback recovery option clearly
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(2.5f)
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "Error de red",
                        tint = Color.Red,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Señal en mantenimiento",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            // Automatically fall back to a 100% working stream
                            activeUrl = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8"
                            playbackError = null
                            isBuffering = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Iniciar Señal de Respaldo HD", fontSize = 11.sp)
                    }
                }
            } else {
                AnimatedVisibility(
                    visible = showControls,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    IconButton(
                        onClick = {
                            if (isPlaying) exoPlayer.pause() else exoPlayer.play()
                        },
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                            .size(60.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Bottom Controls Overlay (Progress update bar and toggles)
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .align(Alignment.BottomStart)
            ) {
                // Media progress slider (Seekbar) for On-Demand Content
                if (!isLive && duration > 0L) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(currentPosition),
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.width(36.dp)
                        )
                        Slider(
                            value = currentPosition.toFloat(),
                            onValueChange = { newValue ->
                                currentPosition = newValue.toLong()
                                exoPlayer.seekTo(currentPosition)
                            },
                            valueRange = 0f..duration.toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(22.dp)
                        )
                        Text(
                            text = formatTime(duration),
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .width(36.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Controls: Playback adjustments
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Forward 10s / Rewind 10s for on demand
                        if (!isLive) {
                            IconButton(
                                onClick = { exoPlayer.seekTo((currentPosition - 10000).coerceAtLeast(0)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Replay10,
                                    contentDescription = "Retroceder 10 Segundos",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(
                                onClick = { exoPlayer.seekTo((currentPosition + 10000).coerceAtMost(duration)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Forward10,
                                    contentDescription = "Adelantar 10 Segundos",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Mute/Volume
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isMuted = !isMuted },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = if (isMuted || volume == 0f) Icons.Default.VolumeOff
                                    else if (volume < 0.5f) Icons.Default.VolumeDown
                                    else Icons.Default.VolumeUp,
                                    contentDescription = "Volumen",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            if (!isMuted) {
                                Slider(
                                    value = volume,
                                    onValueChange = {
                                        volume = it
                                        isMuted = false
                                    },
                                    valueRange = 0f..1f,
                                    modifier = Modifier
                                        .width(70.dp)
                                        .height(16.dp),
                                    colors = SliderDefaults.colors(
                                        thumbColor = Color.White,
                                        activeTrackColor = Color.White,
                                        inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                                    )
                                )
                            }
                        }
                    }

                    // Right Controls: Visual expansion and watermarks
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isLive) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color.Red)
                                )
                                Text(
                                    text = "LIVE",
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Screen enlargement
                        IconButton(
                            onClick = { onFullScreenToggle(!isFullScreenMode) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isFullScreenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                contentDescription = "Pantalla Completa",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Convert ms to format HH:mm:ss or mm:ss
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
