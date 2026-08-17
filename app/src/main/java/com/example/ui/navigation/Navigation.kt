package com.example.ui.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.TranslationManager
import com.example.ui.theme.LiveGreen

sealed class Screen(
  val route: String,
  val titleRes: Int,
  val keyName: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector
) {
  data object Live : Screen(
    route = "live",
    titleRes = R.string.tab_live,
    keyName = "tab_live",
    selectedIcon = Icons.Filled.SportsSoccer,
    unselectedIcon = Icons.Outlined.SportsSoccer
  )

  data object Matches : Screen(
    route = "matches",
    titleRes = R.string.tab_matches,
    keyName = "tab_matches",
    selectedIcon = Icons.Filled.CalendarMonth,
    unselectedIcon = Icons.Outlined.CalendarMonth
  )

  data object Leagues : Screen(
    route = "leagues",
    titleRes = R.string.tab_leagues,
    keyName = "tab_leagues",
    selectedIcon = Icons.Filled.EmojiEvents,
    unselectedIcon = Icons.Outlined.EmojiEvents
  )

  data object Settings : Screen(
    route = "settings",
    titleRes = R.string.tab_settings,
    keyName = "tab_settings",
    selectedIcon = Icons.Filled.Settings,
    unselectedIcon = Icons.Outlined.Settings
  )
}

val bottomNavItems = listOf(
  Screen.Live,
  Screen.Matches,
  Screen.Leagues,
  Screen.Settings
)

@Composable
fun AppBottomNavBar(
  currentRoute: String?,
  onNavigate: (String) -> Unit,
  translationManager: TranslationManager,
  hasLiveMatches: Boolean = true,
  modifier: Modifier = Modifier
) {
  val infiniteTransition = rememberInfiniteTransition(label = "NavLivePulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.8f,
    targetValue = 1.2f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "NavPulseScale"
  )

  Column(modifier = modifier.fillMaxWidth()) {
    HorizontalDivider(
      thickness = 1.dp,
      color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)
    )

    NavigationBar(
      modifier = Modifier.testTag("bottom_nav_bar"),
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.onSurface,
      tonalElevation = 0.dp
    ) {
      bottomNavItems.forEach { screen ->
        val isSelected = currentRoute == screen.route
        val title = translationManager.getString(screen.titleRes, screen.keyName)

        NavigationBarItem(
          selected = isSelected,
          onClick = { onNavigate(screen.route) },
          icon = {
            if (screen == Screen.Live && hasLiveMatches) {
              BadgedBox(
                badge = {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .scale(pulseScale)
                      .clip(CircleShape)
                      .background(LiveGreen)
                  )
                }
              ) {
                Icon(
                  imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                  contentDescription = title,
                  modifier = Modifier.size(24.dp)
                )
              }
            } else {
              Icon(
                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                contentDescription = title,
                modifier = Modifier.size(24.dp)
              )
            }
          },
          label = {
            Text(
              text = title,
              fontSize = 11.sp,
              fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
            )
          },
          colors = NavigationBarItemDefaults.colors(
            selectedIconColor = LiveGreen,
            selectedTextColor = LiveGreen,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            indicatorColor = LiveGreen.copy(alpha = 0.12f)
          ),
          modifier = Modifier.testTag("nav_item_${screen.route}")
        )
      }
    }
  }
}

