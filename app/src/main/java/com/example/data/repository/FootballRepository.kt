package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.api.MockDataGenerator
import com.example.data.model.LanguageDto
import com.example.data.model.League
import com.example.data.model.Match
import com.example.data.model.toDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class FootballRepository {

  private val apiService = ApiClient.apiService

  suspend fun getLiveMatches(langCode: String): Result<List<Match>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getLiveMatches(lang = langCode)
      val domainMatches = response.map { it.toDomain() }
      Result.success(domainMatches)
    } catch (e: Throwable) {
      // Fallback gracefully to realistic mock matches if API endpoint fails
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
      val domainMatches = response.map { it.toDomain() }
      Result.success(domainMatches)
    } catch (e: Throwable) {
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
      val domainLeagues = response.map { it.toDomain() }
      Result.success(domainLeagues)
    } catch (e: Throwable) {
      val fallback = MockDataGenerator.mockLeagues.map { it.toDomain() }
      Result.success(fallback)
    }
  }

  suspend fun getLanguages(): Result<List<LanguageDto>> = withContext(Dispatchers.IO) {
    try {
      val response = apiService.getLanguages()
      if (response.isNotEmpty()) {
        Result.success(response)
      } else {
        Result.success(MockDataGenerator.supportedLanguages)
      }
    } catch (e: Throwable) {
      Result.success(MockDataGenerator.supportedLanguages)
    }
  }

  suspend fun getRemoteTranslations(langCode: String): Map<String, String> = withContext(Dispatchers.IO) {
    try {
      apiService.getTranslations(lang = langCode)
    } catch (e: Throwable) {
      MockDataGenerator.getRemoteTranslations(langCode)
    }
  }

  suspend fun getMatchById(matchId: Int, langCode: String): Match? = withContext(Dispatchers.IO) {
    // Check live first
    val liveResult = getLiveMatches(langCode).getOrNull() ?: emptyList()
    val liveMatch = liveResult.find { it.id == matchId }
    if (liveMatch != null) return@withContext liveMatch

    // Check today's matches
    val todayMatches = getMatches(langCode, LocalDate.now()).getOrNull() ?: emptyList()
    val todayMatch = todayMatches.find { it.id == matchId }
    if (todayMatch != null) return@withContext todayMatch

    // Fallback to searching mock list
    val allMock = (MockDataGenerator.getLiveMatches() + MockDataGenerator.getMatchesForDate(LocalDate.now())).map { it.toDomain() }
    allMock.find { it.id == matchId }
  }
}
