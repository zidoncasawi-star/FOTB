package com.example.data.repository

import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class TranslationManager(
  private val appContext: Context,
  private val repository: FootballRepository,
  private val scope: CoroutineScope
) {

  private val _currentLanguage = MutableStateFlow("en")
  val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

  private val _remoteOverrides = MutableStateFlow<Map<String, String>>(emptyMap())
  val remoteOverrides: StateFlow<Map<String, String>> = _remoteOverrides.asStateFlow()

  val isRtl: Boolean
    get() = _currentLanguage.value.equals("ar", ignoreCase = true)

  fun setLanguage(langCode: String) {
    if (_currentLanguage.value != langCode) {
      _currentLanguage.value = langCode
      fetchRemoteOverrides(langCode)
    }
  }

  fun fetchRemoteOverrides(langCode: String) {
    scope.launch(Dispatchers.IO) {
      val overrides = repository.getRemoteTranslations(langCode)
      _remoteOverrides.value = overrides
    }
  }

  /**
   * Resolves a string with remote override prioritized over localized Android string resources
   */
  fun getString(@StringRes resId: Int, keyName: String? = null, vararg formatArgs: Any): String {
    if (keyName != null) {
      val override = _remoteOverrides.value[keyName]
      if (!override.isNullOrBlank()) {
        return try {
          if (formatArgs.isNotEmpty()) {
            String.format(override, *formatArgs)
          } else {
            override
          }
        } catch (e: Exception) {
          override
        }
      }
    }

    // Load from locale-specific context
    return try {
      val locale = Locale(_currentLanguage.value)
      val config = Configuration(appContext.resources.configuration)
      config.setLocale(locale)
      val localizedContext = appContext.createConfigurationContext(config)
      if (formatArgs.isNotEmpty()) {
        localizedContext.getString(resId, *formatArgs)
      } else {
        localizedContext.getString(resId)
      }
    } catch (e: Exception) {
      try {
        if (formatArgs.isNotEmpty()) {
          appContext.getString(resId, *formatArgs)
        } else {
          appContext.getString(resId)
        }
      } catch (e2: Exception) {
        keyName ?: ""
      }
    }
  }
}
