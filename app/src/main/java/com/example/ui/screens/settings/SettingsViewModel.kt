package com.example.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.LanguageDto
import com.example.data.preferences.ThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.FootballRepository
import com.example.data.repository.TranslationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
  val languages: List<LanguageDto> = emptyList(),
  val selectedLanguageCode: String = "en",
  val themeMode: ThemeMode = ThemeMode.DARK,
  val isBackendConnected: Boolean = true,
  val remoteOverridesCount: Int = 0
)

class SettingsViewModel(
  private val preferencesRepository: UserPreferencesRepository,
  private val footballRepository: FootballRepository,
  private val translationManager: TranslationManager
) : ViewModel() {

  val currentThemeMode: StateFlow<ThemeMode> = preferencesRepository.themeModeFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.DARK)

  val currentLanguageCode: StateFlow<String> = preferencesRepository.selectedLanguageFlow
    .stateIn(viewModelScope, SharingStarted.Eagerly, "en")

  private val _languages = MutableStateFlow<List<LanguageDto>>(emptyList())
  val languages: StateFlow<List<LanguageDto>> = _languages.asStateFlow()

  init {
    loadLanguages()
  }

  fun loadLanguages() {
    viewModelScope.launch {
      val result = footballRepository.getLanguages()
      result.onSuccess { list ->
        _languages.value = list
      }
    }
  }

  fun selectLanguage(code: String) {
    viewModelScope.launch {
      preferencesRepository.setLanguage(code)
      translationManager.setLanguage(code)
    }
  }

  fun selectThemeMode(mode: ThemeMode) {
    viewModelScope.launch {
      preferencesRepository.setThemeMode(mode)
    }
  }

  fun syncTranslations() {
    val lang = currentLanguageCode.value
    translationManager.fetchRemoteOverrides(lang)
  }
}
