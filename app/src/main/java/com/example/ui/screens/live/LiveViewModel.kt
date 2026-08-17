package com.example.ui.screens.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Match
import com.example.data.repository.FootballRepository
import com.example.data.repository.TranslationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LeagueMatches(
  val leagueId: Int,
  val leagueName: String,
  val country: String,
  val matches: List<Match>
)

sealed interface LiveUiState {
  data object Loading : LiveUiState
  data class Success(
    val leagues: List<LeagueMatches>,
    val totalLiveCount: Int,
    val secondsSinceUpdate: Long,
    val isRefreshing: Boolean = false
  ) : LiveUiState
  data class Error(val message: String) : LiveUiState
}

class LiveViewModel(
  private val repository: FootballRepository,
  private val translationManager: TranslationManager
) : ViewModel() {

  private val _uiState = MutableStateFlow<LiveUiState>(LiveUiState.Loading)
  val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

  private var pollingJob: Job? = null
  private var tickerJob: Job? = null
  private var lastUpdateTimestamp: Long = System.currentTimeMillis()

  init {
    startPolling()
    startSecondsTicker()
  }

  fun startPolling() {
    pollingJob?.cancel()
    pollingJob = viewModelScope.launch {
      while (isActive) {
        fetchLiveMatches(isBackgroundRefresh = _uiState.value is LiveUiState.Success)
        delay(15_000) // Poll live.php every 15 seconds
      }
    }
  }

  fun pausePolling() {
    pollingJob?.cancel()
    pollingJob = null
  }

  fun resumePolling() {
    if (pollingJob == null || pollingJob?.isActive == false) {
      startPolling()
    }
  }

  fun refresh() {
    viewModelScope.launch {
      _uiState.update { current ->
        if (current is LiveUiState.Success) current.copy(isRefreshing = true) else current
      }
      fetchLiveMatches(isBackgroundRefresh = false)
    }
  }

  private suspend fun fetchLiveMatches(isBackgroundRefresh: Boolean) {
    val lang = translationManager.currentLanguage.value
    val result = repository.getLiveMatches(lang)

    result.onSuccess { matches ->
      lastUpdateTimestamp = System.currentTimeMillis()
      // Group by league
      val grouped = matches.groupBy { it.leagueId }
        .map { (leagueId, leagueMatchesList) ->
          val first = leagueMatchesList.first()
          LeagueMatches(
            leagueId = leagueId,
            leagueName = first.leagueName,
            country = first.country,
            matches = leagueMatchesList
          )
        }

      _uiState.value = LiveUiState.Success(
        leagues = grouped,
        totalLiveCount = matches.count { it.isLive },
        secondsSinceUpdate = 0,
        isRefreshing = false
      )
    }.onFailure { err ->
      if (!isBackgroundRefresh || _uiState.value !is LiveUiState.Success) {
        _uiState.value = LiveUiState.Error(err.localizedMessage ?: "Failed to load live scores")
      } else {
        _uiState.update { current ->
          if (current is LiveUiState.Success) current.copy(isRefreshing = false) else current
        }
      }
    }
  }

  private fun startSecondsTicker() {
    tickerJob?.cancel()
    tickerJob = viewModelScope.launch {
      while (isActive) {
        delay(1000)
        val elapsedSeconds = (System.currentTimeMillis() - lastUpdateTimestamp) / 1000
        _uiState.update { current ->
          if (current is LiveUiState.Success) {
            current.copy(secondsSinceUpdate = elapsedSeconds)
          } else {
            current
          }
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    pollingJob?.cancel()
    tickerJob?.cancel()
  }
}
