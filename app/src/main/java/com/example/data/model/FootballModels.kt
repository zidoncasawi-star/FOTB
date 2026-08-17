package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

@JsonClass(generateAdapter = true)
data class MatchDto(
  @Json(name = "id") val id: Int,
  @Json(name = "status") val status: String,
  @Json(name = "match_minute") val matchMinute: String? = null,
  @Json(name = "home_score") val homeScore: Int? = null,
  @Json(name = "away_score") val awayScore: Int? = null,
  @Json(name = "match_time") val matchTime: String, // "YYYY-MM-DD HH:mm:ss" UTC
  @Json(name = "league_id") val leagueId: Int,
  @Json(name = "league_name") val leagueName: String,
  @Json(name = "country") val country: String? = null,
  @Json(name = "home_id") val homeId: Int,
  @Json(name = "home_name") val homeName: String,
  @Json(name = "home_logo") val homeLogo: String? = null,
  @Json(name = "away_id") val awayId: Int,
  @Json(name = "away_name") val awayName: String,
  @Json(name = "away_logo") val awayLogo: String? = null
)

@JsonClass(generateAdapter = true)
data class LeagueDto(
  @Json(name = "id") val id: Int,
  @Json(name = "name") val name: String,
  @Json(name = "country") val country: String? = null,
  @Json(name = "logo_url") val logoUrl: String? = null
)

@JsonClass(generateAdapter = true)
data class LanguageDto(
  @Json(name = "code") val code: String,
  @Json(name = "name") val name: String,
  @Json(name = "dir") val dir: String? = "ltr",
  @Json(name = "default") val isDefault: Boolean? = false
)

// The backend wraps every list response in an envelope object rather than
// returning a bare JSON array/map - these mirror that exact shape.
@JsonClass(generateAdapter = true)
data class MatchesEnvelope(
  @Json(name = "lang") val lang: String? = null,
  @Json(name = "matches") val matches: List<MatchDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LeaguesEnvelope(
  @Json(name = "lang") val lang: String? = null,
  @Json(name = "leagues") val leagues: List<LeagueDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class LanguagesEnvelope(
  @Json(name = "languages") val languages: List<LanguageDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TranslationsEnvelope(
  @Json(name = "lang") val lang: String? = null,
  @Json(name = "strings") val strings: Map<String, String> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class TeamStatisticsDto(
  @Json(name = "possession_pct") val possessionPct: Int? = null,
  @Json(name = "shots_on_target") val shotsOnTarget: Int? = null,
  @Json(name = "shots_off_target") val shotsOffTarget: Int? = null,
  @Json(name = "corners") val corners: Int? = null,
  @Json(name = "fouls") val fouls: Int? = null,
  @Json(name = "offsides") val offsides: Int? = null,
  @Json(name = "yellow_cards") val yellowCards: Int? = null,
  @Json(name = "red_cards") val redCards: Int? = null
)

@JsonClass(generateAdapter = true)
data class MatchStatisticsDto(
  @Json(name = "home") val home: TeamStatisticsDto? = null,
  @Json(name = "away") val away: TeamStatisticsDto? = null
)

@JsonClass(generateAdapter = true)
data class MatchIncidentDto(
  @Json(name = "minute") val minute: Int? = null,
  @Json(name = "team") val team: String, // "home" | "away"
  @Json(name = "type") val type: String, // "goal" | "card" | "substitution"
  @Json(name = "player") val player: String? = null,
  @Json(name = "player_in") val playerIn: String? = null,
  @Json(name = "player_out") val playerOut: String? = null
)

@JsonClass(generateAdapter = true)
data class LineupPlayerDto(
  @Json(name = "name") val name: String? = null,
  @Json(name = "position") val position: String? = null,
  @Json(name = "number") val number: Int? = null
)

@JsonClass(generateAdapter = true)
data class MatchLineupsDto(
  @Json(name = "home") val home: List<LineupPlayerDto>? = null,
  @Json(name = "away") val away: List<LineupPlayerDto>? = null
)

@JsonClass(generateAdapter = true)
data class MatchDetailsEnvelope(
  @Json(name = "lang") val lang: String? = null,
  @Json(name = "match") val match: MatchDto,
  @Json(name = "statistics") val statistics: MatchStatisticsDto? = null,
  @Json(name = "incidents") val incidents: List<MatchIncidentDto> = emptyList(),
  @Json(name = "lineups") val lineups: MatchLineupsDto? = null,
  @Json(name = "details_updated_at") val detailsUpdatedAt: String? = null
)

enum class MatchStatus(val apiValue: String) {
  SCHEDULED("scheduled"),
  LIVE("live"),
  HALFTIME("halftime"),
  FINISHED("finished"),
  POSTPONED("postponed"),
  CANCELLED("cancelled");

  companion object {
    fun fromString(value: String): MatchStatus {
      return entries.find { it.apiValue.equals(value, ignoreCase = true) } ?: SCHEDULED
    }
  }
}

data class Match(
  val id: Int,
  val status: MatchStatus,
  val matchMinute: String?,
  val homeScore: Int?,
  val awayScore: Int?,
  val utcMatchTime: String,
  val localKickoffTime: String,
  val localKickoffDate: String,
  val leagueId: Int,
  val leagueName: String,
  val country: String,
  val homeId: Int,
  val homeName: String,
  val homeLogo: String?,
  val awayId: Int,
  val awayName: String,
  val awayLogo: String?
) {
  val isLive: Boolean get() = status == MatchStatus.LIVE || status == MatchStatus.HALFTIME
  val isFinished: Boolean get() = status == MatchStatus.FINISHED
  val isScheduled: Boolean get() = status == MatchStatus.SCHEDULED
}

fun MatchDto.toDomain(): Match {
  val (localDate, localTime) = formatUtcToLocal(matchTime)
  return Match(
    id = id,
    status = MatchStatus.fromString(status),
    matchMinute = matchMinute,
    homeScore = homeScore,
    awayScore = awayScore,
    utcMatchTime = matchTime,
    localKickoffTime = localTime,
    localKickoffDate = localDate,
    leagueId = leagueId,
    leagueName = leagueName,
    country = country ?: "International",
    homeId = homeId,
    homeName = homeName,
    homeLogo = homeLogo,
    awayId = awayId,
    awayName = awayName,
    awayLogo = awayLogo
  )
}

fun LeagueDto.toDomain(): League {
  return League(
    id = id,
    name = name,
    country = country ?: "International",
    logoUrl = logoUrl
  )
}

data class League(
  val id: Int,
  val name: String,
  val country: String,
  val logoUrl: String?
)

data class MatchDetails(
  val match: Match,
  val statistics: MatchStatisticsDto?,
  val incidents: List<MatchIncidentDto>,
  val lineups: MatchLineupsDto?
) {
  val hasStatistics: Boolean get() = statistics?.home != null || statistics?.away != null
  val hasLineups: Boolean get() = !lineups?.home.isNullOrEmpty() || !lineups?.away.isNullOrEmpty()
}

fun MatchDetailsEnvelope.toDomain(): MatchDetails = MatchDetails(
  match = match.toDomain(),
  statistics = statistics,
  incidents = incidents,
  lineups = lineups
)

/**
 * Converts UTC "YYYY-MM-DD HH:mm:ss" string to Local Device Date and Time formatted strings
 */
private fun formatUtcToLocal(utcString: String): Pair<String, String> {
  return try {
    val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    val parsedUtc = LocalDateTime.parse(utcString.trim(), utcFormatter)
    val utcZoned = parsedUtc.atZone(ZoneOffset.UTC)
    val localZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault())
    
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    
    Pair(localZoned.format(dateFormatter), localZoned.format(timeFormatter))
  } catch (e: Exception) {
    // Fallback if format is slightly different
    val parts = utcString.split(" ")
    val datePart = parts.getOrNull(0) ?: "Today"
    val timePart = parts.getOrNull(1)?.take(5) ?: "19:00"
    Pair(datePart, timePart)
  }
}
