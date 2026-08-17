package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Match
import com.example.data.model.MatchStatus
import com.example.ui.theme.HalftimeYellow
import com.example.ui.theme.LiveGreen
import com.example.ui.theme.LiveRed

@Composable
fun MatchCard(
  match: Match,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isLive = match.isLive
  val isHalftime = match.status == MatchStatus.HALFTIME
  val isFinished = match.isFinished
  val isScheduled = match.isScheduled

  val isHomeWinning = (match.homeScore ?: 0) > (match.awayScore ?: 0) && (isLive || isFinished)
  val isAwayWinning = (match.awayScore ?: 0) > (match.homeScore ?: 0) && (isLive || isFinished)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 5.dp)
      .clip(RoundedCornerShape(16.dp))
      .background(MaterialTheme.colorScheme.surface)
      .border(
        width = 1.dp,
        color = if (isLive) LiveGreen.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp)
      )
      .clickable(onClick = onClick)
      .testTag("match_card_${match.id}")
  ) {
    // Left Accent Strip for Live Matches
    if (isLive) {
      Box(
        modifier = Modifier
          .align(Alignment.CenterStart)
          .width(4.dp)
          .fillMaxHeight()
          .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
          .background(LiveGreen)
      )
    }

    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
      // Top Status Bar inside Card
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        when {
          isLive -> {
            LiveBadge(
              minute = match.matchMinute ?: "LIVE",
              isHalftime = isHalftime
            )
          }
          isFinished -> {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "FULL TIME",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            }
          }
          isScheduled -> {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "${match.localKickoffTime}  •  ${match.localKickoffDate}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
          match.status == MatchStatus.POSTPONED -> {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "POSTPONED",
                color = LiveRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          match.status == MatchStatus.CANCELLED -> {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "CANCELLED",
                color = LiveRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
          else -> {
            Text(
              text = match.localKickoffTime,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 11.sp
            )
          }
        }

        // Half indicator badge if live
        if (isLive) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(MaterialTheme.colorScheme.background)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = if (isHalftime) "HT" else if ((match.matchMinute?.filter { it.isDigit() }?.toIntOrNull() ?: 0) > 45) "2H" else "1H",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Teams and Center Scoreboard (Professional Polish 3-Column Layout)
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        // Home Team (Left Column)
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            TeamLogo(name = match.homeName, logoUrl = match.homeLogo, size = 26.dp)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = match.homeName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isHomeWinning) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isHomeWinning) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isAwayWinning) 0.65f else 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }

        // Center Scoreboard Column
        Column(
          modifier = Modifier.padding(horizontal = 12.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          if (isLive || isFinished) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = "${match.homeScore ?: 0}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = if (isHomeWinning) LiveGreen else MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "-",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
              )
              Text(
                text = "${match.awayScore ?: 0}",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = if (isAwayWinning) LiveGreen else MaterialTheme.colorScheme.onSurface
              )
            }
          } else {
            Text(
              text = match.localKickoffTime,
              fontSize = 18.sp,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "VS",
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
              letterSpacing = 1.sp
            )
          }
        }

        // Away Team (Right Column)
        Column(
          modifier = Modifier.weight(1f),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.Center
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            TeamLogo(name = match.awayName, logoUrl = match.awayLogo, size = 26.dp)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = match.awayName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isAwayWinning) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isAwayWinning) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurface.copy(alpha = if (isHomeWinning) 0.65f else 0.9f),
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    }
  }
}

