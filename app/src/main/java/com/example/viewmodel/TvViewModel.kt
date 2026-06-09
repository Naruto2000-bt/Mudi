package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TvViewModel : ViewModel() {

    // Tab Navigation State: 0 = En Vivo, 1 = Bajo Demanda, 2 = Mundial 2026
    private val _currentTab = MutableStateFlow(0)
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Currently selected channel or local video details for the video player
    private val _selectedChannel = MutableStateFlow<Channel?>(TvRepository.channels.first())
    val selectedChannel: StateFlow<Channel?> = _selectedChannel.asStateFlow()

    private val _selectedOnDemand = MutableStateFlow<OnDemandContent?>(null)
    val selectedOnDemand: StateFlow<OnDemandContent?> = _selectedOnDemand.asStateFlow()

    private val _playbackUrl = MutableStateFlow(TvRepository.channels.first().streamUrl)
    val playbackUrl: StateFlow<String> = _playbackUrl.asStateFlow()

    private val _playbackTitle = MutableStateFlow("Teletica Canal 7 - Telenoticias Edición Matutina")
    val playbackTitle: StateFlow<String> = _playbackTitle.asStateFlow()

    private val _isLivePlayback = MutableStateFlow(true)
    val isLivePlayback: StateFlow<Boolean> = _isLivePlayback.asStateFlow()

    // EPG (Programming Guide) filter
    private val _selectedChannelCategory = MutableStateFlow("Todos")
    val selectedChannelCategory: StateFlow<String> = _selectedChannelCategory.asStateFlow()

    // On Demand category filter
    private val _selectedOnDemandCategory = MutableStateFlow("Todos")
    val selectedOnDemandCategory: StateFlow<String> = _selectedOnDemandCategory.asStateFlow()

    // Favorites list
    private val _favoriteChannelIds = MutableStateFlow<Set<String>>(setOf("teletica_7", "teletica_deportes"))
    val favoriteChannelIds: StateFlow<Set<String>> = _favoriteChannelIds.asStateFlow()

    // Matches state - supporting live updates
    private val _worldCupMatches = MutableStateFlow(TvRepository.worldCupMatches)
    val worldCupMatches: StateFlow<List<WorldCupMatch>> = _worldCupMatches.asStateFlow()

    init {
        // Run a simulated background task to update World Cup live match events organically
        viewModelScope.launch {
            while (true) {
                delay(12000) // update every 12 seconds
                _worldCupMatches.value = _worldCupMatches.value.map { match ->
                    if (match.status == "EN_VIVO") {
                        // Randomly add minutes or tick scores
                        val currentMinute = match.minute?.removeSuffix("'")?.toIntOrNull() ?: 78
                        val nextMinute = if (currentMinute >= 90) 90 else currentMinute + 1
                        
                        // Small chance of scoring a goal
                        val randomChance = (1..100).random()
                        var currentScoreA = match.scoreA ?: 2
                        var currentScoreB = match.scoreB ?: 1
                        if (randomChance > 85) {
                            if ((0..1).random() == 0) {
                                currentScoreA += 1
                            } else {
                                currentScoreB += 1
                            }
                        }
                        match.copy(
                            minute = "$nextMinute'",
                            scoreA = currentScoreA,
                            scoreB = currentScoreB,
                            status = if (nextMinute >= 90) "FINALIZADO" else "EN_VIVO"
                        )
                    } else {
                        match
                    }
                }
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    // Load Live Channel into Player
    fun playChannel(channel: Channel) {
        _selectedChannel.value = channel
        _selectedOnDemand.value = null
        _playbackUrl.value = channel.streamUrl
        _isLivePlayback.value = true

        // Find the current live program for this channel if available
        val currentHourStr = getCurrentHourStr()
        val epg = TvRepository.programsByChannel[channel.id] ?: emptyList()
        val currentProgram = epg.find { prog ->
            isCurrentProgram(prog.startTime, prog.endTime, currentHourStr)
        } ?: epg.firstOrNull()

        _playbackTitle.value = if (currentProgram != null) {
            "${channel.name} - ${currentProgram.title}"
        } else {
            channel.name
        }
    }

    // Load On Demand Video into Player
    fun playOnDemand(content: OnDemandContent) {
        _selectedOnDemand.value = content
        _selectedChannel.value = null
        _playbackUrl.value = content.videoUrl
        _playbackTitle.value = "${content.category}: ${content.title}"
        _isLivePlayback.value = false
        
        // When user plays local contents, automatically bring up the player tab (En Vivo holds the player view)
        // or let them watch right in place!
        _currentTab.value = 0
    }

    // Triggered from World Cup Match layout
    fun playMatchBroadcaster(match: WorldCupMatch) {
        val broadcasterId = match.broadcasterChannelIds.firstOrNull() ?: "teletica_deportes"
        val channel = TvRepository.channels.find { it.id == broadcasterId } ?: TvRepository.channels.first()
        
        _selectedChannel.value = channel
        _selectedOnDemand.value = null
        _playbackUrl.value = channel.streamUrl
        _isLivePlayback.value = true
        _playbackTitle.value = "Mundial 2026: ${match.flagA} ${match.teamA} vs ${match.teamB} ${match.flagB} en vivo por ${channel.name}"

        // Switch to main stream tab
        _currentTab.value = 0
    }

    fun toggleFavorite(channelId: String) {
        val current = _favoriteChannelIds.value.toMutableSet()
        if (current.contains(channelId)) {
            current.remove(channelId)
        } else {
            current.add(channelId)
        }
        _favoriteChannelIds.value = current
    }

    fun setChannelCategory(category: String) {
        _selectedChannelCategory.value = category
    }

    fun setOnDemandCategory(category: String) {
        _selectedOnDemandCategory.value = category
    }

    // Helper functions for Costa Rica dates and times
    private fun getCurrentHourStr(): String {
        // Generates simple hour indicator for mock matching (standard 14:15 style)
        // In real apps, we read from Calendar.getInstance()
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }

    private fun isCurrentProgram(start: String, end: String, current: String): Boolean {
        return try {
            val startVal = start.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            val endVal = end.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            val curVal = current.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            if (endVal < startVal) {
                // overnight show
                curVal >= startVal || curVal < endVal
            } else {
                curVal in startVal until endVal
            }
        } catch (e: Exception) {
            false
        }
    }
}
