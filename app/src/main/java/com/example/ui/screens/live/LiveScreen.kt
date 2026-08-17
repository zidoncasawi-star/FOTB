package com.example.ui.screens.live

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.R
import com.example.data.repository.TranslationManager
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorState
import com.example.ui.common.LeagueHeader
import com.example.ui.common.MatchCard
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.LiveGreenContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
  viewModel: LiveViewModel,
  translationManager: TranslationManager,
  onMatchClick: (Int) -> Unit,
  onNavigateToMatches: () -> Unit,
  onLeagueClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val lifecycleOwner = LocalLifecycleOwner.current

  // Pause polling when screen goes into background, resume when foregrounded
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> viewModel.resumePolling()
        Lifecycle.Event.ON_PAUSE -> viewModel.pausePolling()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Neon Green Accent Bar
            Box(
              modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(LiveGreen)
            )

            Text(
              text = translationManager.getString(R.string.app_name, "app_name"),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onBackground
            )

            if (uiState is LiveUiState.Success) {
              val count = (uiState as LiveUiState.Success).totalLiveCount
              if (count > 0) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(LiveGreenContainer)
                    .padding(horizontal = 7.dp, vertical = 2.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "$count LIVE",
                    color = LiveGreen,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                  )
                }
              }
            }
          }
        },
        actions = {
          if (uiState is LiveUiState.Success) {
            val seconds = (uiState as LiveUiState.Success).secondsSinceUpdate
            val timeText = if (seconds < 5) {
              translationManager.getString(R.string.updated_just_now, "updated_just_now")
            } else if (seconds < 60) {
              translationManager.getString(R.string.updated_seconds_ago, "updated_seconds_ago", seconds)
            } else {
              translationManager.getString(R.string.updated_minutes_ago, "updated_minutes_ago", seconds / 60)
            }

            Text(
              text = timeText,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(end = 6.dp)
            )
          }

          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.surface)
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
          ) {
            IconButton(
              onClick = { viewModel.refresh() },
              modifier = Modifier
                .size(38.dp)
                .testTag("refresh_live_button")
            ) {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = translationManager.getString(R.string.refresh, "refresh"),
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    }
  ) { paddingValues ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      when (val state = uiState) {
        is LiveUiState.Loading -> {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            CircularProgressIndicator(
              color = LiveGreen,
              strokeWidth = 3.dp
            )
          }
        }
        is LiveUiState.Error -> {
          ErrorState(
            title = translationManager.getString(R.string.error_loading, "error_loading"),
            description = state.message,
            retryLabel = translationManager.getString(R.string.retry, "retry"),
            onRetry = { viewModel.refresh() }
          )
        }
        is LiveUiState.Success -> {
          PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
          ) {
            if (state.leagues.isEmpty()) {
              EmptyState(
                title = translationManager.getString(R.string.no_live_matches, "no_live_matches"),
                description = translationManager.getString(R.string.no_live_matches_desc, "no_live_matches_desc"),
                actionLabel = translationManager.getString(R.string.view_today_matches, "view_today_matches"),
                onActionClick = onNavigateToMatches
              )
            } else {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
              ) {
                state.leagues.forEach { leagueMatches ->
                  item(key = "header_${leagueMatches.leagueId}") {
                    LeagueHeader(
                      leagueName = leagueMatches.leagueName,
                      country = leagueMatches.country,
                      onClick = { onLeagueClick(leagueMatches.leagueId) }
                    )
                  }

                  items(leagueMatches.matches, key = { it.id }) { match ->
                    MatchCard(
                      match = match,
                      onClick = { onMatchClick(match.id) }
                    )
                  }

                  item(key = "spacer_${leagueMatches.leagueId}") {
                    Spacer(modifier = Modifier.height(10.dp))
                  }
                }

                item(key = "bottom_padding") {
                  Spacer(modifier = Modifier.height(16.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}
