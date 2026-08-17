package com.example.ui.screens.matches

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.TranslationManager
import com.example.ui.common.DateStrip
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorState
import com.example.ui.common.LeagueHeader
import com.example.ui.common.MatchCard
import com.example.ui.theme.LiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesScreen(
  viewModel: MatchesViewModel,
  translationManager: TranslationManager,
  onMatchClick: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val selectedDate by viewModel.selectedDate.collectAsState()
  val selectedFilter by viewModel.selectedFilter.collectAsState()

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(LiveGreen)
            )

            Text(
              text = translationManager.getString(R.string.tab_matches, "tab_matches"),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        },
        actions = {
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
                .testTag("refresh_matches_button")
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
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Horizontal Date Picker
      DateStrip(
        selectedDate = selectedDate,
        onDateSelected = { date -> viewModel.selectDate(date) },
        todayLabel = translationManager.getString(R.string.today, "today"),
        yesterdayLabel = translationManager.getString(R.string.yesterday, "yesterday"),
        tomorrowLabel = translationManager.getString(R.string.tomorrow, "tomorrow")
      )

      // Filter Chips Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState())
          .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Status filter chips
        MatchFilter.entries.forEach { filter ->
          val label = when (filter) {
            MatchFilter.ALL -> translationManager.getString(R.string.filter_all, "filter_all")
            MatchFilter.LIVE -> translationManager.getString(R.string.filter_live, "filter_live")
            MatchFilter.FINISHED -> translationManager.getString(R.string.filter_finished, "filter_finished")
            MatchFilter.UPCOMING -> translationManager.getString(R.string.filter_upcoming, "filter_upcoming")
          }
          val isSelected = selectedFilter == filter

            FilterChip(
            selected = isSelected,
            onClick = { viewModel.selectFilter(filter) },
            label = {
              Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold
              )
            },
            shape = RoundedCornerShape(10.dp),
            border = FilterChipDefaults.filterChipBorder(
              enabled = true,
              selected = isSelected,
              borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
              selectedBorderColor = LiveGreen
            ),
            colors = FilterChipDefaults.filterChipColors(
              containerColor = MaterialTheme.colorScheme.surface,
              labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
              selectedContainerColor = LiveGreen,
              selectedLabelColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.testTag("filter_chip_${filter.name}")
          )
        }

        // Active League Filter Chip if present
        if (uiState is MatchesUiState.Success) {
          val leagueName = (uiState as MatchesUiState.Success).selectedLeagueName
          if (!leagueName.isNullOrBlank()) {
            InputChip(
              selected = true,
              onClick = { viewModel.clearLeagueFilter() },
              label = { Text(text = leagueName, fontWeight = FontWeight.Bold) },
              trailingIcon = {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear league filter",
                  modifier = Modifier.size(16.dp)
                )
              },
              colors = InputChipDefaults.inputChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
              ),
              modifier = Modifier.testTag("active_league_chip")
            )
          }
        }
      }

      // Match Content Area
      Box(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
      ) {
        when (val state = uiState) {
          is MatchesUiState.Loading -> {
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
          is MatchesUiState.Error -> {
            ErrorState(
              title = translationManager.getString(R.string.error_loading, "error_loading"),
              description = state.message,
              retryLabel = translationManager.getString(R.string.retry, "retry"),
              onRetry = { viewModel.refresh() }
            )
          }
          is MatchesUiState.Success -> {
            PullToRefreshBox(
              isRefreshing = state.isRefreshing,
              onRefresh = { viewModel.refresh() },
              modifier = Modifier.fillMaxSize()
            ) {
              if (state.leagues.isEmpty()) {
                EmptyState(
                  title = translationManager.getString(R.string.no_matches_found, "no_matches_found"),
                  description = translationManager.getString(R.string.no_matches_found_desc, "no_matches_found_desc")
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
                        onClick = {
                          viewModel.selectLeague(leagueMatches.leagueId, leagueMatches.leagueName)
                        }
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

                  item(key = "bottom_space") {
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
}
