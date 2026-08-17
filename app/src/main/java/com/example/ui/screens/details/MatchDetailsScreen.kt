package com.example.ui.screens.details

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.data.repository.TranslationManager
import com.example.ui.common.ErrorState
import com.example.ui.common.LiveBadge
import com.example.ui.common.TeamLogo
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentSky
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.LiveRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailsScreen(
  viewModel: MatchDetailsViewModel,
  translationManager: TranslationManager,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var selectedTabIndex by remember { mutableIntStateOf(0) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = translationManager.getString(R.string.match_details, "match_details"),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag("back_button")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
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
        is MatchDetailsUiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = LiveGreen, strokeWidth = 3.dp)
          }
        }
        is MatchDetailsUiState.Error -> {
          ErrorState(
            title = "Match Details Unavailable",
            description = state.message,
            retryLabel = translationManager.getString(R.string.retry, "retry"),
            onRetry = { viewModel.loadMatchDetails() }
          )
        }
        is MatchDetailsUiState.Success -> {
          val match = state.match
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
          ) {
            // Match Header Banner / Scoreboard
            MatchHeaderScoreboard(
              match = match,
              translationManager = translationManager
            )

            // Primary Tabs: Overview, Stats, Lineups, H2H
            val tabs = listOf(
              translationManager.getString(R.string.match_overview, "match_overview"),
              translationManager.getString(R.string.match_stats, "match_stats"),
              translationManager.getString(R.string.match_lineups, "match_lineups"),
              translationManager.getString(R.string.match_h2h, "match_h2h")
            )

            PrimaryTabRow(
              selectedTabIndex = selectedTabIndex,
              containerColor = MaterialTheme.colorScheme.surface,
              contentColor = LiveGreen,
              divider = {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
              }
            ) {
              tabs.forEachIndexed { index, title ->
                Tab(
                  selected = selectedTabIndex == index,
                  onClick = { selectedTabIndex = index },
                  text = {
                    Text(
                      text = title,
                      fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 13.sp
                    )
                  }
                )
              }
            }

            // Tab Content
            Box(modifier = Modifier.padding(16.dp)) {
              when (selectedTabIndex) {
                0 -> MatchOverviewSection(match = match, translationManager = translationManager)
                1 -> MatchStatsSection(match = match, translationManager = translationManager)
                2 -> MatchLineupsSection(match = match, translationManager = translationManager)
                3 -> MatchH2HSection(match = match, translationManager = translationManager)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MatchHeaderScoreboard(
  match: Match,
  translationManager: TranslationManager,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.background
          )
        )
      )
      .padding(16.dp)
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.fillMaxWidth()
    ) {
      // League and Round
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        Icon(
          imageVector = Icons.Default.EmojiEvents,
          contentDescription = null,
          tint = AccentGold,
          modifier = Modifier.size(16.dp)
        )
        Text(
          text = "${match.leagueName} • ${match.country}",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Teams and Scoreboard
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Home Team Column
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          TeamLogo(
            name = match.homeName,
            logoUrl = match.homeLogo,
            size = 56.dp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = match.homeName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Center Score / Status
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.padding(horizontal = 8.dp)
        ) {
          if (match.isLive || match.isFinished) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "${match.homeScore ?: 0}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "-",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = "${match.awayScore ?: 0}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          } else {
            Text(
              text = match.localKickoffTime,
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          when {
            match.isLive -> {
              LiveBadge(
                minute = match.matchMinute ?: "LIVE",
                isHalftime = match.status == MatchStatus.HALFTIME
              )
            }
            match.isFinished -> {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .padding(horizontal = 8.dp, vertical = 2.dp)
              ) {
                Text(
                  text = translationManager.getString(R.string.status_finished, "status_finished"),
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            else -> {
              Text(
                text = match.localKickoffDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Away Team Column
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.weight(1f)
        ) {
          TeamLogo(
            name = match.awayName,
            logoUrl = match.awayLogo,
            size = 56.dp
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = match.awayName,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

@Composable
private fun MatchOverviewSection(
  match: Match,
  translationManager: TranslationManager
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    // Info Card
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        .padding(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        DetailRow(
          icon = Icons.Default.CalendarMonth,
          label = translationManager.getString(R.string.kickoff, "kickoff"),
          value = "${match.localKickoffDate} • ${match.localKickoffTime} (Local Time)"
        )
        DetailRow(
          icon = Icons.Default.LocationOn,
          label = translationManager.getString(R.string.stadium, "stadium"),
          value = "${match.homeName} Stadium, ${match.country}"
        )
        DetailRow(
          icon = Icons.Default.Person,
          label = translationManager.getString(R.string.referee, "referee"),
          value = "M. Oliver (Official)"
        )
      }
    }

    // Match Timeline Placeholder
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        .padding(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
          text = "Key Match Events",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )

        if (match.isLive || match.isFinished) {
          EventItem(minute = "23'", player = "Goal! ${match.homeName} (#9)", isHome = true, isGoal = true)
          EventItem(minute = "41'", player = "Yellow Card (#4)", isHome = false, isGoal = false)
          if ((match.awayScore ?: 0) > 0) {
            EventItem(minute = "58'", player = "Goal! ${match.awayName} (#10)", isHome = false, isGoal = true)
          }
          if ((match.homeScore ?: 0) > 1) {
            EventItem(minute = "65'", player = "Goal! ${match.homeName} (#7)", isHome = true, isGoal = true)
          }
        } else {
          Text(
            text = "Match has not started yet. Live events and goals will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.size(18.dp)
    )
    Column {
      Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
  }
}

@Composable
private fun EventItem(minute: String, player: String, isHome: Boolean, isGoal: Boolean) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = if (isHome) Arrangement.Start else Arrangement.End
  ) {
    Box(
      modifier = Modifier
        .clip(RoundedCornerShape(6.dp))
        .background(if (isGoal) LiveGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant)
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = minute, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (isGoal) LiveGreen else MaterialTheme.colorScheme.onSurface)
        Text(text = player, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }
}

@Composable
private fun MatchStatsSection(
  match: Match,
  translationManager: TranslationManager
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
      Text(
        text = translationManager.getString(R.string.match_stats, "match_stats"),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      StatBar(
        label = translationManager.getString(R.string.possession, "possession"),
        homeValue = "58%",
        awayValue = "42%",
        homeRatio = 0.58f
      )

      StatBar(
        label = translationManager.getString(R.string.shots_on_target, "shots_on_target"),
        homeValue = "7",
        awayValue = "4",
        homeRatio = 7f / 11f
      )

      StatBar(
        label = translationManager.getString(R.string.total_shots, "total_shots"),
        homeValue = "14",
        awayValue = "9",
        homeRatio = 14f / 23f
      )

      StatBar(
        label = translationManager.getString(R.string.corners, "corners"),
        homeValue = "6",
        awayValue = "3",
        homeRatio = 6f / 9f
      )

      StatBar(
        label = translationManager.getString(R.string.fouls, "fouls"),
        homeValue = "11",
        awayValue = "13",
        homeRatio = 11f / 24f
      )

      StatBar(
        label = translationManager.getString(R.string.yellow_cards, "yellow_cards"),
        homeValue = "2",
        awayValue = "3",
        homeRatio = 2f / 5f
      )
    }
  }
}

@Composable
private fun StatBar(
  label: String,
  homeValue: String,
  awayValue: String,
  homeRatio: Float
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(text = homeValue, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = LiveGreen)
      Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(text = awayValue, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentSky)
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
      Box(
        modifier = Modifier
          .weight(homeRatio.coerceIn(0.05f, 0.95f))
          .fillMaxSize()
          .background(LiveGreen)
      )
      Box(
        modifier = Modifier
          .width(2.dp)
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.surface)
      )
      Box(
        modifier = Modifier
          .weight((1f - homeRatio).coerceIn(0.05f, 0.95f))
          .fillMaxSize()
          .background(AccentSky)
      )
    }
  }
}

@Composable
private fun MatchLineupsSection(
  match: Match,
  translationManager: TranslationManager
) {
  Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(MaterialTheme.colorScheme.surface)
        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
        .padding(16.dp)
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(text = match.homeName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Formation: 4-3-3", style = MaterialTheme.typography.bodySmall, color = LiveGreen)
          }
          Column(horizontalAlignment = Alignment.End) {
            Text(text = match.awayName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Formation: 4-2-3-1", style = MaterialTheme.typography.bodySmall, color = AccentSky)
          }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 4.dp))

        Text(
          text = translationManager.getString(R.string.starting_xi, "starting_xi"),
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val samplePlayers = listOf(
          Pair("1. Raya (GK)", "1. Sánchez (GK)"),
          Pair("4. White", "24. James (C)"),
          Pair("2. Saliba", "6. Silva"),
          Pair("6. Gabriel", "26. Colwill"),
          Pair("35. Zinchenko", "3. Cucurella"),
          Pair("41. Rice", "25. Caicedo"),
          Pair("8. Ødegaard (C)", "8. Fernández"),
          Pair("29. Havertz", "23. Gallagher"),
          Pair("7. Saka", "20. Palmer"),
          Pair("9. Jesus", "15. Jackson"),
          Pair("11. Martinelli", "7. Sterling")
        )

        samplePlayers.forEach { (homeP, awayP) ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = homeP, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = awayP, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }
  }
}

@Composable
private fun MatchH2HSection(
  match: Match,
  translationManager: TranslationManager
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(MaterialTheme.colorScheme.surface)
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      .padding(16.dp)
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(
        text = "Head-to-Head History",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      val h2h = listOf(
        Triple("2024-04-23", "${match.homeName} 5 - 0 ${match.awayName}", "Premier League"),
        Triple("2023-10-21", "${match.awayName} 2 - 2 ${match.homeName}", "Premier League"),
        Triple("2023-05-02", "${match.homeName} 3 - 1 ${match.awayName}", "Premier League"),
        Triple("2022-11-06", "${match.awayName} 0 - 1 ${match.homeName}", "Premier League")
      )

      h2h.forEach { (date, score, comp) ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(text = score, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "$comp • $date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}
