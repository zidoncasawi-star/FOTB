package com.example.data.api

import com.example.data.model.LanguagesEnvelope
import com.example.data.model.LeaguesEnvelope
import com.example.data.model.MatchDetailsEnvelope
import com.example.data.model.MatchesEnvelope
import com.example.data.model.TranslationsEnvelope
import retrofit2.http.GET
import retrofit2.http.Query

interface FootballApiService {

  @GET("live.php")
  suspend fun getLiveMatches(
    @Query("lang") lang: String
  ): MatchesEnvelope

  @GET("matches.php")
  suspend fun getMatches(
    @Query("lang") lang: String,
    @Query("date") date: String? = null,
    @Query("league_id") leagueId: Int? = null,
    @Query("status") status: String? = null
  ): MatchesEnvelope

  @GET("leagues.php")
  suspend fun getLeagues(
    @Query("lang") lang: String
  ): LeaguesEnvelope

  @GET("languages.php")
  suspend fun getLanguages(): LanguagesEnvelope

  @GET("translations.php")
  suspend fun getTranslations(
    @Query("lang") lang: String
  ): TranslationsEnvelope

  @GET("match_details.php")
  suspend fun getMatchDetails(
    @Query("id") id: Int,
    @Query("lang") lang: String
  ): MatchDetailsEnvelope
}
