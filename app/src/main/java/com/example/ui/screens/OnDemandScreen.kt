package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
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
import com.example.data.OnDemandContent
import com.example.data.TvRepository
import com.example.viewmodel.TvViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnDemandScreen(
    viewModel: TvViewModel,
    modifier: Modifier = Modifier
) {
    val items = TvRepository.onDemandItems
    val activeCategory by viewModel.selectedOnDemandCategory.collectAsState()
    var detailToShow by remember { mutableStateOf<OnDemandContent?>(null) }

    // Filter items
    val filteredItems = remember(activeCategory) {
        if (activeCategory == "Todos") {
            items
        } else {
            items.filter { it.category.equals(activeCategory, ignoreCase = true) }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121214))
    ) {
        // Categories Row
        val categories = listOf("Todos", "Comedia", "Cine Tico", "Documentales", "Cultura")
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
                    onClick = { viewModel.setOnDemandCategory(category) },
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
                    modifier = Modifier.testTag("ondemand_chip_$category"),
                    border = null,
                    shape = RoundedCornerShape(20.dp)
                )
            }
        }

        // On-Demand Catalog Grid Items
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron producciones locales",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 80.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredItems, key = { it.id }) { content ->
                    OnDemandGridItem(
                        content = content,
                        onClick = { detailToShow = content }
                    )
                }
            }
        }

        // Beautiful details Bottom Sheet panel when clicked
        detailToShow?.let { content ->
            ModalBottomSheet(
                onDismissRequest = { detailToShow = null },
                containerColor = Color(0xFF1D1B20),
                contentColor = Color.White,
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                OnDemandDetailSheetContent(
                    content = content,
                    onPlayClick = {
                        viewModel.playOnDemand(content)
                        detailToShow = null
                    },
                    onDismiss = { detailToShow = null }
                )
            }
        }
    }
}

@Composable
fun OnDemandGridItem(
    content: OnDemandContent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(205.dp)
            .clickable { onClick() }
            .testTag("ondemand_item_${content.id}"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16161A)
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Thumbnail poster
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(content.thumbnail),
                    contentDescription = content.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Category overlay tag
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = content.category,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Duration badge bottom right
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(3.dp),
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Text(
                        text = content.duration,
                        fontSize = 9.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Title and summaries
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = content.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = content.description,
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${content.year}  •",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Surface(
                        color = Color(0xFF29292E),
                        shape = RoundedCornerShape(2.dp)
                    ) {
                        Text(
                            text = content.rating,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnDemandDetailSheetContent(
    content: OnDemandContent,
    onPlayClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Details Poster
            Image(
                painter = rememberAsyncImagePainter(content.thumbnail),
                contentDescription = content.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(width = 110.dp, height = 150.dp)
                    .clip(RoundedCornerShape(8.dp))
            )

            // Right Info Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = content.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = content.category,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "${content.year}  •  ${content.duration}",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF323038),
                        shape = RoundedCornerShape(3.dp)
                    ) {
                        Text(
                            text = "Clasif: ${content.rating}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.LightGray,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "1080p Full HD",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Outline / Synopsis
        Text(
            text = "Sinopsis",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = content.description,
            fontSize = 12.sp,
            color = Color.LightGray,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Play Button Action
        Button(
            onClick = onPlayClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sheet_play_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "REPRODUCIR EN ALTA DEFINICIÓN",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}
