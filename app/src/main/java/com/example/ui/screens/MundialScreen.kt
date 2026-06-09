package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.data.TvRepository
import com.example.data.WorldCupMatch
import com.example.viewmodel.TvViewModel

@Composable
fun MundialScreen(
    viewModel: TvViewModel,
    modifier: Modifier = Modifier
) {
    val matches by viewModel.worldCupMatches.collectAsState()
    val channels = TvRepository.channels.filter { it.isWorldCupBroadcaster }
    val groupStandings = TvRepository.groupStandings

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF121214)),
        contentPadding = PaddingValues(top = 10.dp, start = 12.dp, end = 12.dp, bottom = 90.dp)
    ) {
        // Broadcasters Section
        item {
            SectionTitle(title = "Señales Oficiales del Mundial", icon = Icons.Default.LiveTv)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                items(channels, key = { it.id }) { channel ->
                    BroadcasterPill(
                        channelName = channel.name,
                        logoUrl = channel.logoUrl,
                        freq = channel.frequency,
                        onClick = { viewModel.playChannel(channel) }
                    )
                }
            }
        }

        // Match schedule section
        item {
            SectionTitle(title = "Calendario de Partidos (Hora de Costa Rica)", icon = Icons.Default.SportsSoccer)
            Spacer(modifier = Modifier.height(10.dp))
        }

        items(matches, key = { it.id }) { match ->
            MatchCardItem(
                match = match,
                onStreamClick = { viewModel.playMatchBroadcaster(match) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Scoreboard Group Stage section
        item {
            Spacer(modifier = Modifier.height(12.dp))
            SectionTitle(title = "Tabla de Posiciones - Grupo E", icon = Icons.Default.Update)
            Spacer(modifier = Modifier.height(10.dp))
            StandingsTable(groupStandings = groupStandings)
        }
    }
}

@Composable
fun SectionTitle(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun BroadcasterPill(
    channelName: String,
    logoUrl: String,
    freq: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFF1C1B1F),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .width(180.dp)
            .clickable { onClick() }
            .testTag("broadcaster_pill_$channelName"),
        border = BorderStroke(1.dp, Color(0xFF29292E))
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(logoUrl),
                    contentDescription = channelName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = channelName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = freq,
                    fontSize = 9.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MatchCardItem(
    match: WorldCupMatch,
    onStreamClick: () -> Unit
) {
    val isLive = match.status == "EN_VIVO"
    val isCompleted = match.status == "FINALIZADO"

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isLive) Color(0xFF1E281F) else Color(0xFF16161A)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isLive) Color.Red.copy(alpha = 0.4f) else Color(0xFF29292E),
                RoundedCornerShape(12.dp)
            )
            .testTag("match_card_${match.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header stats (Group, Status pill, Stadium, Location)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.group,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Match status Badge
                Surface(
                    color = when {
                        isLive -> Color.Red
                        isCompleted -> Color(0xFF29292E)
                        else -> Color(0xFF202024)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = when {
                            isLive -> "AL AIRE • ${match.minute}"
                            isCompleted -> "FINALIZADO"
                            else -> "PROXIMO"
                        },
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Central Fixture: Team - Flags - Scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Team A (Costa Rica or other)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(match.flagA, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.teamA,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }

                // Scores Ticker OR Hours CST Costa Rica
                if (isLive || isCompleted) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = (match.scoreA ?: 0).toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = "-",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = (match.scoreB ?: 0).toString(),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = match.timeCR,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Hora CR CST",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Team B
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp)
                ) {
                    Text(match.flagB, fontSize = 28.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = match.teamB,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stadium & Date footer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = match.date,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = match.stadium,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(180.dp)
                    )
                }

                // Play Stream direct shortcut link
                if (isLive || !isCompleted) {
                    Button(
                        onClick = onStreamClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLive) "VER LIVE HD" else "SINTONIZAR PREVIA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StandingsTable(
    groupStandings: List<com.example.data.StandingRow>
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF16161A)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF29292E), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Equipo", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                Text("PJ", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                Text("PG", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                Text("DG", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                Text("PTS", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
            }
            Divider(color = Color(0xFF29292E), thickness = 1.dp)

            // Rows
            groupStandings.forEachIndexed { index, row ->
                val isCostaRica = row.team.contains("Costa Rica")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isCostaRica) Color(0xFF1E281F) else Color.Transparent)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${index + 1}.  ${row.team}",
                        color = if (isCostaRica) MaterialTheme.colorScheme.primary else Color.White,
                        fontSize = 12.sp,
                        fontWeight = if (isCostaRica) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(2f)
                    )
                    Text(text = row.played.toString(), color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                    Text(text = row.wins.toString(), color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(0.5f), textAlign = TextAlign.Center)
                    Text(text = row.goalDiff, color = Color.LightGray, fontSize = 11.sp, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
                    Text(
                        text = row.points.toString(),
                        color = if (isCostaRica) MaterialTheme.colorScheme.primary else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(0.6f),
                        textAlign = TextAlign.Center
                    )
                }
                if (index < groupStandings.size - 1) {
                    Divider(color = Color(0xFF1E1E22), thickness = 1.dp)
                }
            }
        }
    }
}
