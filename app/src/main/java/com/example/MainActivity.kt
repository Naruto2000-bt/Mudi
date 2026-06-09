package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VideoPlayer
import com.example.ui.screens.LiveScreen
import com.example.ui.screens.MundialScreen
import com.example.ui.screens.OnDemandScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.TvViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TvViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Collect reactive state flows from our ViewModel
                val currentTab by viewModel.currentTab.collectAsState()
                val selectedChannel by viewModel.selectedChannel.collectAsState()
                val selectedOnDemand by viewModel.selectedOnDemand.collectAsState()
                val playbackUrl by viewModel.playbackUrl.collectAsState()
                val playbackTitle by viewModel.playbackTitle.collectAsState()
                val isLivePlayback by viewModel.isLivePlayback.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "🇨🇷 TV COSTA RICA",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "HD",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = Color(0xFF121214),
                                titleContentColor = Color.White
                            )
                        )
                    },
                    bottomBar = {
                        // Standard M3 Navigation Bar with proper system container insets
                        NavigationBar(
                            modifier = Modifier
                                .testTag("bottom_nav_bar")
                                .windowInsetsPadding(WindowInsets.navigationBars),
                            containerColor = Color(0xFF16161A),
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { viewModel.selectTab(0) },
                                icon = { Icon(Icons.Default.LiveTv, contentDescription = "En Vivo") },
                                label = { Text("En Vivo", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = Color.LightGray,
                                    unselectedTextColor = Color.LightGray,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("tab_live_tv")
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { viewModel.selectTab(1) },
                                icon = { Icon(Icons.Default.Movie, contentDescription = "Bajo Demanda") },
                                label = { Text("Bajo Demanda", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = Color.LightGray,
                                    unselectedTextColor = Color.LightGray,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("tab_on_demand")
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { viewModel.selectTab(2) },
                                icon = { Icon(Icons.Default.SportsSoccer, contentDescription = "Mundial 2026") },
                                label = { Text("Mundial", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = Color.LightGray,
                                    unselectedTextColor = Color.LightGray,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ),
                                modifier = Modifier.testTag("tab_mundial")
                            )
                        }
                    },
                    containerColor = Color(0xFF121214)
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        // Global Media Stream Player Header
                        // Holds the streaming session active and smooth
                        VideoPlayer(
                            videoUrl = playbackUrl,
                            title = playbackTitle,
                            isLive = isLivePlayback,
                            backupUrl = if (selectedChannel != null) selectedChannel!!.backupUrl else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black),
                            isFullScreenMode = false
                        )

                        // Screen sliding switcher for distinct tabs
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            when (currentTab) {
                                0 -> LiveScreen(viewModel = viewModel)
                                1 -> OnDemandScreen(viewModel = viewModel)
                                2 -> MundialScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
