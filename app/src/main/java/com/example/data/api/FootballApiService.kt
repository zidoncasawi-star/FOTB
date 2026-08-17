package com.example.data.api

import com.example.data.model.LanguageDto
import com.example.data.model.LeagueDto
import com.example.data.model.MatchDto
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

  @GET("live.php")
  suspend fun getLiveMatches(
    @Query("lang") lang: String
  ): List<MatchDto>

  @GET("matches.php")
  suspend fun getMatches(
    @Query("lang") lang: String,
    @Query("date") date: String? = null,
    @Query("league_id") leagueId: Int? = null,
    @Query("status") status: String? = null
  ): List<MatchDto>

  @GET("leagues.php")
  suspend fun getLeagues(
    @Query("lang") lang: String
  ): List<LeagueDto>

  @GET("languages.php")
  suspend fun getLanguages(): List<LanguageDto>

  @GET("translations.php")
  suspend fun getTranslations(
    @Query("lang") lang: String
  ): Map<String, String>
}
