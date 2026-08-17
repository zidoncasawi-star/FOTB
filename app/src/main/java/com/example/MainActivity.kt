package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FootballRepository
import com.example.data.repository.TranslationManager
import com.example.ui.navigation.AppBottomNavBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.details.MatchDetailsScreen
import com.example.ui.screens.details.MatchDetailsViewModel
import com.example.ui.screens.leagues.LeaguesScreen
import com.example.ui.screens.leagues.LeaguesViewModel
import com.example.ui.screens.live.LiveScreen
import com.example.ui.screens.live.LiveViewModel
import com.example.ui.screens.matches.MatchesScreen
import com.example.ui.screens.matches.MatchesViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.theme.FootballTodayTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val scope = rememberCoroutineScope()
      val userPrefs = remember { UserPreferencesRepository(applicationContext) }
      val footballRepo = remember { FootballRepository() }
      val translationManager = remember { TranslationManager(applicationContext, footballRepo, scope) }

      val themeMode by userPrefs.themeModeFlow.collectAsState(initial = ThemeMode.DARK)
      val selectedLanguage by userPrefs.selectedLanguageFlow.collectAsState(initial = "en")

      LaunchedEffect(selectedLanguage) {
        translationManager.setLanguage(selectedLanguage)
      }

      val systemDark = isSystemInDarkTheme()
      val isDarkTheme = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
      }

      val layoutDirection = if (selectedLanguage.equals("ar", ignoreCase = true)) {
        LayoutDirection.Rtl
      } else {
        LayoutDirection.Ltr
      }

      FootballTodayTheme(darkTheme = isDarkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
          FootballAppContent(
            footballRepository = footballRepo,
            userPreferencesRepository = userPrefs,
            translationManager = translationManager
          )
        }
      }
    }
  }
}

@Composable
fun FootballAppContent(
  footballRepository: FootballRepository,
  userPreferencesRepository: UserPreferencesRepository,
  translationManager: TranslationManager
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val liveViewModel = remember { LiveViewModel(footballRepository, translationManager) }
  val matchesViewModel = remember { MatchesViewModel(footballRepository, translationManager) }
  val leaguesViewModel = remember { LeaguesViewModel(footballRepository, translationManager) }
  val settingsViewModel = remember { SettingsViewModel(userPreferencesRepository, footballRepository, translationManager) }

  // Re-trigger loads when language updates
  val currentLang by translationManager.currentLanguage.collectAsState()
  LaunchedEffect(currentLang) {
    liveViewModel.refresh()
    matchesViewModel.loadMatches(isRefresh = true)
    leaguesViewModel.loadLeagues()
  }

  val isDetailsRoute = currentRoute?.startsWith("match_details") == true

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    bottomBar = {
      if (!isDetailsRoute) {
        AppBottomNavBar(
          currentRoute = currentRoute ?: Screen.Live.route,
          onNavigate = { route ->
            navController.navigate(route) {
              popUpTo(Screen.Live.route) {
                saveState = true
              }
              launchSingleTop = true
              restoreState = true
            }
          },
          translationManager = translationManager
        )
      }
    }
  ) { innerPadding ->
    NavHost(
      navController = navController,
      startDestination = Screen.Live.route,
      modifier = Modifier.padding(innerPadding)
    ) {
      // 1. Live Screen
      composable(Screen.Live.route) {
        LiveScreen(
          viewModel = liveViewModel,
          translationManager = translationManager,
          onMatchClick = { matchId ->
            navController.navigate("match_details/$matchId")
          },
          onNavigateToMatches = {
            navController.navigate(Screen.Matches.route) {
              popUpTo(Screen.Live.route) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          },
          onLeagueClick = { leagueId ->
            matchesViewModel.selectLeague(leagueId, null)
            navController.navigate(Screen.Matches.route) {
              popUpTo(Screen.Live.route) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
      }

      // 2. Matches Screen
      composable(Screen.Matches.route) {
        MatchesScreen(
          viewModel = matchesViewModel,
          translationManager = translationManager,
          onMatchClick = { matchId ->
            navController.navigate("match_details/$matchId")
          }
        )
      }

      // 3. Leagues Screen
      composable(Screen.Leagues.route) {
        LeaguesScreen(
          viewModel = leaguesViewModel,
          translationManager = translationManager,
          onLeagueSelected = { leagueId, leagueName ->
            matchesViewModel.selectLeague(leagueId, leagueName)
            navController.navigate(Screen.Matches.route) {
              popUpTo(Screen.Live.route) { saveState = true }
              launchSingleTop = true
              restoreState = true
            }
          }
        )
      }

      // 4. Settings Screen
      composable(Screen.Settings.route) {
        SettingsScreen(
          viewModel = settingsViewModel,
          translationManager = translationManager
        )
      }

      // 5. Match Details Screen
      composable(
        route = "match_details/{matchId}",
        arguments = listOf(navArgument("matchId") { type = NavType.IntType })
      ) { backStackEntry ->
        val matchId = backStackEntry.arguments?.getInt("matchId") ?: 0
        val detailsViewModel = remember(matchId) {
          MatchDetailsViewModel(matchId, footballRepository, translationManager)
        }
        MatchDetailsScreen(
          viewModel = detailsViewModel,
          translationManager = translationManager,
          onNavigateBack = { navController.popBackStack() }
        )
      }
    }
  }
}

