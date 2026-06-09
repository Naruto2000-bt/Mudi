package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.Channel
import com.example.data.Program
import com.example.data.TvRepository
import com.example.viewmodel.TvViewModel

@Composable
fun LiveScreen(
    viewModel: TvViewModel,
    modifier: Modifier = Modifier
) {
    val channels = TvRepository.channels
    val selectedChannel by viewModel.selectedChannel.collectAsState()
    val favoriteIds by viewModel.favoriteChannelIds.collectAsState()
    val activeCategory by viewModel.selectedChannelCategory.collectAsState()

    // Filtered channels
    val filteredChannels = remember(activeCategory, favoriteIds) {
        channels.filter { channel ->
            when (activeCategory) {
                "Todos" -> true
                "Favoritos" -> favoriteIds.contains(channel.id)
                else -> channel.category.equals(activeCategory, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        // Channel Category Chips (Horizontal Scroll)
        val categories = listOf("Todos", "Favoritos", "Nacional", "Deportes", "Cultural", "Regional")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 12.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == activeCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { viewModel.setChannelCategory(category) },
                    label = { 
                        Text(
                            text = category,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ) 
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = Color(0xFF202024),
                        labelColor = Color.LightGray,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("category_chip_$category"),
                    border = null,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // Live Channel Horizontal List Carousel
        if (filteredChannels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No hay canales agregados en esta categoría",
                    color = Color.Gray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                items(filteredChannels, key = { it.id }) { channel ->
                    val isSelected = channel.id == selectedChannel?.id
                    ChannelCarouselItem(
                        channel = channel,
                        isSelected = isSelected,
                        isFavorite = favoriteIds.contains(channel.id),
                        onClick = { viewModel.playChannel(channel) },
                        onFavoriteToggle = { viewModel.toggleFavorite(channel.id) }
                    )
                }
            }
        }

        Divider(color = Color(0xFF29292E), thickness = 1.dp)

        // EPB Programming Guide Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Tv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Guía de Programación (EPG)",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = Color(0xFF202024),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "Hora Costa Rica (CST)",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Selected Channel EPG Programs Table List
        if (selectedChannel != null) {
            val programs = TvRepository.programsByChannel[selectedChannel!!.id] ?: emptyList()
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(programs, key = { it.id }) { program ->
                    EpgProgramItem(
                        program = program,
                        isCurrentlyPlaying = program.isLive
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Seleccione un canal arriba para ver su programación.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ChannelCarouselItem(
    channel: Channel,
    isSelected: Boolean,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(136.dp)
            .height(115.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF29292E),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .testTag("channel_item_${channel.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF202024) else Color(0xFF16161A)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Channel Logo / Emblem
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    val painter = rememberAsyncImagePainter(
                        model = channel.logoUrl,
                        error = rememberAsyncImagePainter("https://images.unsplash.com/photo-1542204172-e7052809a86e?auto=format&fit=crop&w=100")
                    )
                    Image(
                        painter = painter,
                        contentDescription = "Logo de ${channel.name}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(2.dp)
                    )
                }

                // Name and Freq
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = channel.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = channel.frequency,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }
            }

            // Small Favorite Badge Clicker
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { onFavoriteToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorito",
                    tint = if (isFavorite) Color.Red else Color.LightGray,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

@Composable
fun EpgProgramItem(
    program: Program,
    isCurrentlyPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isCurrentlyPlaying) Color(0xFF1E291F) else Color.Transparent)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Time Box Block
        Column(
            modifier = Modifier.width(62.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = program.startTime,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else Color.White
            )
            Text(
                text = "a ${program.endTime}",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content description
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = program.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentlyPlaying) MaterialTheme.colorScheme.primary else Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Live blinking icon indicator
                if (isCurrentlyPlaying) {
                    Surface(
                        color = Color.Red,
                        shape = RoundedCornerShape(3.dp),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = "AL AIRE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = program.description,
                fontSize = 11.sp,
                color = Color.LightGray,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (isCurrentlyPlaying) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = 0.55f, // Simulated current timeline progress
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
            }
        }
    }
    Divider(color = Color(0xFF1E1E22), thickness = 1.dp)
}
