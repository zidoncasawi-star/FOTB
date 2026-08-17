package com.example.data.repository

import android.util.Log
import com.example.data.api.ApiClient
import com.example.data.api.MockDataGenerator
import com.example.data.model.LanguageDto
import com.example.data.model.League
import com.example.data.model.Match
import com.example.data.model.MatchDetails
import com.example.data.model.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val TAG = "FootballRepository"

class FootballRepository {

  private val apiService = ApiClient.apiService

  suspend fun getLiveMatches(langCode: String): Result<List<Match>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getLiveMatches(lang = langCode)
      Result.success(response.matches.map { it.toDomain() })
    } catch (e: Throwable) {
      Log.w(TAG, "getLiveMatches failed, falling back to mock data", e)
      val fallback = MockDataGenerator.getLiveMatches().map { it.toDomain() }
      Result.success(fallback)
    }
  }

  suspend fun getMatches(
    langCode: String,
    date: LocalDate,
    leagueId: Int? = null,
    status: String? = null
  ): Result<List<Match>> = withContext(Dispatchers.IO) {
    try {
      val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US))
      val response = apiService.getMatches(
        lang = langCode,
        date = dateStr,
        leagueId = leagueId,
        status = status
      )
      Result.success(response.matches.map { it.toDomain() })
    } catch (e: Throwable) {
      Log.w(TAG, "getMatches failed, falling back to mock data", e)
      // Filter mock matches by date, league, and status
      val allDateMatches = MockDataGenerator.getMatchesForDate(date).map { it.toDomain() }
      val filtered = allDateMatches.filter { match ->
        val matchesLeague = leagueId == null || match.leagueId == leagueId
        val matchesStatus = status == null || status.equals("all", ignoreCase = true) || match.status.apiValue.equals(status, ignoreCase = true)
        matchesLeague && matchesStatus
      }
      Result.success(filtered)
    }
  }

  suspend fun getLeagues(langCode: String): Result<List<League>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getLeagues(lang = langCode)
      Result.success(response.leagues.map { it.toDomain() })
    } catch (e: Throwable) {
      Log.w(TAG, "getLeagues failed, falling back to mock data", e)
      val fallback = MockDataGenerator.mockLeagues.map { it.toDomain() }
      Result.success(fallback)
    }
  }

  suspend fun getLanguages(): Result<List<LanguageDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getLanguages()
      if (response.languages.isNotEmpty()) {
        Result.success(response.languages)
      } else {
        Result.success(MockDataGenerator.supportedLanguages)
      }
    } catch (e: Throwable) {
      Log.w(TAG, "getLanguages failed, falling back to bundled defaults", e)
      Result.success(MockDataGenerator.supportedLanguages)
    }
  }

  suspend fun getRemoteTranslations(langCode: String): Map<String, String> = withContext(Dispatchers.IO) {
    try {
      apiService.getTranslations(lang = langCode).strings
    } catch (e: Throwable) {
      Log.w(TAG, "getRemoteTranslations failed, falling back to bundled defaults", e)
      MockDataGenerator.getRemoteTranslations(langCode)
    }
  }

  suspend fun getMatchDetails(matchId: Int, langCode: String): Result<MatchDetails> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getMatchDetails(id = matchId, lang = langCode)
      Result.success(response.toDomain())
    } catch (e: Throwable) {
      Log.w(TAG, "getMatchDetails failed for id=$matchId", e)
      Result.failure(e)
    }
  }
}
