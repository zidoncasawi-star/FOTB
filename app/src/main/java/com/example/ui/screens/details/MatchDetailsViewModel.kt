package com.example.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.MatchDetails
import com.example.data.repository.FootballRepository
import com.example.data.repository.TranslationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MatchDetailsUiState {
  data object Loading : MatchDetailsUiState
  data class Success(val details: MatchDetails) : MatchDetailsUiState
  data class Error(val message: String) : MatchDetailsUiState
}

class MatchDetailsViewModel(
  private val matchId: Int,
  private val repository: FootballRepository,
  private val translationManager: TranslationManager
) : ViewModel() {

  private val _uiState = MutableStateFlow<MatchDetailsUiState>(MatchDetailsUiState.Loading)
  val uiState: StateFlow<MatchDetailsUiState> = _uiState.asStateFlow()

  init {
    loadMatchDetails()
  }

  fun loadMatchDetails() {
    viewModelScope.launch {
      _uiState.value = MatchDetailsUiState.Loading
      val lang = translationManager.currentLanguage.value
      repository.getMatchDetails(matchId, lang)
        .onSuccess { details -> _uiState.value = MatchDetailsUiState.Success(details) }
        .onFailure { err -> _uiState.value = MatchDetailsUiState.Error(err.localizedMessage ?: "Match not found") }
    }
  }
}
