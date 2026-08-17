package com.example.ui.screens.leagues

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.League
import com.example.data.repository.FootballRepository
import com.example.data.repository.TranslationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CountryLeagues(
  val country: String,
  val leagues: List<League>
)

sealed interface LeaguesUiState {
  data object Loading : LeaguesUiState
  data class Success(
    val countryGroups: List<CountryLeagues>,
    val searchQuery: String = ""
  ) : LeaguesUiState
  data class Error(val message: String) : LeaguesUiState
}

class LeaguesViewModel(
  private val repository: FootballRepository,
  private val translationManager: TranslationManager
) : ViewModel() {

  private val _uiState = MutableStateFlow<LeaguesUiState>(LeaguesUiState.Loading)
  val uiState: StateFlow<LeaguesUiState> = _uiState.asStateFlow()

  private var allLeagues: List<League> = emptyList()
  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  init {
    loadLeagues()
  }

  fun loadLeagues() {
    viewModelScope.launch {
      _uiState.value = LeaguesUiState.Loading
      val lang = translationManager.currentLanguage.value
      val result = repository.getLeagues(lang)

      result.onSuccess { leagues ->
        allLeagues = leagues
        filterLeagues(_searchQuery.value)
      }.onFailure { err ->
        _uiState.value = LeaguesUiState.Error(err.localizedMessage ?: "Failed to load leagues")
      }
    }
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
    filterLeagues(query)
  }

  private fun filterLeagues(query: String) {
    val filtered = if (query.isBlank()) {
      allLeagues
    } else {
      allLeagues.filter {
        it.name.contains(query, ignoreCase = true) || it.country.contains(query, ignoreCase = true)
      }
    }

    val grouped = filtered.groupBy { it.country }
      .map { (country, leaguesList) ->
        CountryLeagues(country = country, leagues = leaguesList)
      }

    _uiState.value = LeaguesUiState.Success(
      countryGroups = grouped,
      searchQuery = query
    )
  }
}
