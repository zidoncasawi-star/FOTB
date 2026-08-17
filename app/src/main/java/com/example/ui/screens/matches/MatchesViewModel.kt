package com.example.ui.screens.matches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Match
import com.example.data.repository.FootballRepository
import com.example.data.repository.TranslationManager
import com.example.ui.screens.live.LeagueMatches
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class MatchFilter {
  ALL,
  LIVE,
  FINISHED,
  UPCOMING
}

sealed interface MatchesUiState {
  data object Loading : MatchesUiState
  data class Success(
    val leagues: List<LeagueMatches>,
    val selectedDate: LocalDate,
    val selectedFilter: MatchFilter,
    val selectedLeagueId: Int? = null,
    val selectedLeagueName: String? = null,
    val isRefreshing: Boolean = false
  ) : MatchesUiState
  data class Error(val message: String) : MatchesUiState
}

class MatchesViewModel(
  private val repository: FootballRepository,
  private val translationManager: TranslationManager
) : ViewModel() {

  private val _selectedDate = MutableStateFlow(LocalDate.now())
  val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

  private val _selectedFilter = MutableStateFlow(MatchFilter.ALL)
  val selectedFilter: StateFlow<MatchFilter> = _selectedFilter.asStateFlow()

  private val _selectedLeagueId = MutableStateFlow<Int?>(null)
  val selectedLeagueId: StateFlow<Int?> = _selectedLeagueId.asStateFlow()

  private val _selectedLeagueName = MutableStateFlow<String?>(null)
  val selectedLeagueName: StateFlow<String?> = _selectedLeagueName.asStateFlow()

  private val _uiState = MutableStateFlow<MatchesUiState>(MatchesUiState.Loading)
  val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

  init {
    loadMatches()
  }

  fun selectDate(date: LocalDate) {
    _selectedDate.value = date
    loadMatches()
  }

  fun selectFilter(filter: MatchFilter) {
    _selectedFilter.value = filter
    loadMatches()
  }

  fun selectLeague(leagueId: Int?, leagueName: String?) {
    _selectedLeagueId.value = leagueId
    _selectedLeagueName.value = leagueName
    loadMatches()
  }

  fun clearLeagueFilter() {
    _selectedLeagueId.value = null
    _selectedLeagueName.value = null
    loadMatches()
  }

  fun refresh() {
    viewModelScope.launch {
      _uiState.update { current ->
        if (current is MatchesUiState.Success) current.copy(isRefreshing = true) else current
      }
      loadMatches(isRefresh = true)
    }
  }

  fun loadMatches(isRefresh: Boolean = false) {
    viewModelScope.launch {
      if (!isRefresh && _uiState.value !is MatchesUiState.Success) {
        _uiState.value = MatchesUiState.Loading
      }

      val lang = translationManager.currentLanguage.value
      val date = _selectedDate.value
      val leagueId = _selectedLeagueId.value
      val filter = _selectedFilter.value

      val statusQuery = when (filter) {
        MatchFilter.ALL -> null
        MatchFilter.LIVE -> "live"
        MatchFilter.FINISHED -> "finished"
        MatchFilter.UPCOMING -> "scheduled"
      }

      val result = repository.getMatches(
        langCode = lang,
        date = date,
        leagueId = leagueId,
        status = statusQuery
      )

      result.onSuccess { matches ->
        // Additional in-memory filtering in case backend returns broader list
        val filtered = matches.filter { match ->
          when (filter) {
            MatchFilter.ALL -> true
            MatchFilter.LIVE -> match.isLive
            MatchFilter.FINISHED -> match.isFinished
            MatchFilter.UPCOMING -> match.isScheduled
          }
        }

        val grouped = filtered.groupBy { it.leagueId }
          .map { (id, leagueMatches) ->
            val first = leagueMatches.first()
            LeagueMatches(
              leagueId = id,
              leagueName = first.leagueName,
              country = first.country,
              matches = leagueMatches
            )
          }

        _uiState.value = MatchesUiState.Success(
          leagues = grouped,
          selectedDate = date,
          selectedFilter = filter,
          selectedLeagueId = leagueId,
          selectedLeagueName = _selectedLeagueName.value,
          isRefreshing = false
        )
      }.onFailure { err ->
        _uiState.value = MatchesUiState.Error(err.localizedMessage ?: "Failed to load matches")
      }
    }
  }
}
