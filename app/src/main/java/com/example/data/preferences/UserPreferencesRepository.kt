package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "football_today_prefs")

enum class ThemeMode {
  DARK,
  LIGHT,
  SYSTEM
}

class UserPreferencesRepository(private val context: Context) {

  private object PreferencesKeys {
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    val THEME_MODE = stringPreferencesKey("theme_mode")
  }

  val selectedLanguageFlow: Flow<String> = context.dataStore.data
    .map { preferences ->
      preferences[PreferencesKeys.SELECTED_LANGUAGE] ?: "en"
    }

  val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
    .map { preferences ->
      val value = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.DARK.name
      try {
        ThemeMode.valueOf(value)
      } catch (e: Exception) {
        ThemeMode.DARK
      }
    }

  suspend fun setLanguage(languageCode: String) {
    context.dataStore.edit { preferences ->
      preferences[PreferencesKeys.SELECTED_LANGUAGE] = languageCode
    }
  }

  suspend fun setThemeMode(mode: ThemeMode) {
    context.dataStore.edit { preferences ->
      preferences[PreferencesKeys.THEME_MODE] = mode.name
    }
  }
}
