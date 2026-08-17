package com.example.data.api

import com.example.data.model.LanguageDto
import com.example.data.model.LeagueDto
import com.example.data.model.MatchDto
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object MockDataGenerator {

  val supportedLanguages = listOf(
    LanguageDto(code = "en", name = "English", dir = "ltr", isDefault = true),
    LanguageDto(code = "es", name = "Español", dir = "ltr", isDefault = false),
    LanguageDto(code = "de", name = "Deutsch", dir = "ltr", isDefault = false),
    LanguageDto(code = "fr", name = "Français", dir = "ltr", isDefault = false),
    LanguageDto(code = "nl", name = "Nederlands", dir = "ltr", isDefault = false),
    LanguageDto(code = "it", name = "Italiano", dir = "ltr", isDefault = false),
    LanguageDto(code = "pt", name = "Português", dir = "ltr", isDefault = false),
    LanguageDto(code = "ar", name = "العربية", dir = "rtl", isDefault = false)
  )

  val mockLeagues = listOf(
    LeagueDto(id = 1, name = "Premier League", country = "England", logoUrl = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=100"),
    LeagueDto(id = 2, name = "UEFA Champions League", country = "Europe", logoUrl = "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=100"),
    LeagueDto(id = 3, name = "La Liga", country = "Spain", logoUrl = "https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=100"),
    LeagueDto(id = 4, name = "Serie A", country = "Italy", logoUrl = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=100"),
    LeagueDto(id = 5, name = "Bundesliga", country = "Germany", logoUrl = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=100"),
    LeagueDto(id = 6, name = "Ligue 1", country = "France", logoUrl = "https://images.unsplash.com/photo-1511886929837-354d827aae26?w=100"),
    LeagueDto(id = 7, name = "Eredivisie", country = "Netherlands", logoUrl = "https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?w=100"),
    LeagueDto(id = 8, name = "Primeira Liga", country = "Portugal", logoUrl = "https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?w=100")
  )

  fun getLiveMatches(): List<MatchDto> {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.US)
    val todayStr = now.format(formatter)

    return listOf(
      MatchDto(
        id = 101,
        status = "live",
        matchMinute = "67'",
        homeScore = 2,
        awayScore = 1,
        matchTime = todayStr,
        leagueId = 1,
        leagueName = "Premier League",
        country = "England",
        homeId = 1,
        homeName = "Arsenal",
        homeLogo = "https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=100",
        awayId = 2,
        awayName = "Chelsea",
        awayLogo = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?w=100"
      ),
      MatchDto(
        id = 102,
        status = "live",
        matchMinute = "83'",
        homeScore = 3,
        awayScore = 2,
        matchTime = todayStr,
        leagueId = 2,
        leagueName = "UEFA Champions League",
        country = "Europe",
        homeId = 3,
        homeName = "Real Madrid",
        homeLogo = "https://images.unsplash.com/photo-1522778119026-d647f0596c20?w=100",
        awayId = 4,
        awayName = "Bayern München",
        awayLogo = "https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=100"
      ),
      MatchDto(
        id = 103,
        status = "halftime",
        matchMinute = "HT",
        homeScore = 1,
        awayScore = 0,
        matchTime = todayStr,
        leagueId = 3,
        leagueName = "La Liga",
        country = "Spain",
        homeId = 5,
        homeName = "Barcelona",
        homeLogo = "https://images.unsplash.com/photo-1518091043644-c1d4457512c6?w=100",
        awayId = 6,
        awayName = "Atlético Madrid",
        awayLogo = "https://images.unsplash.com/photo-1511886929837-354d827aae26?w=100"
      ),
      MatchDto(
        id = 104,
        status = "live",
        matchMinute = "24'",
        homeScore = 0,
        awayScore = 0,
        matchTime = todayStr,
        leagueId = 4,
        leagueName = "Serie A",
        country = "Italy",
        homeId = 7,
        homeName = "Inter Milan",
        homeLogo = "https://images.unsplash.com/photo-1489944440615-453fc2b6a9a9?w=100",
        awayId = 8,
        awayName = "Juventus",
        awayLogo = "https://images.unsplash.com/photo-1431324155629-1a6deb1dec8d?w=100"
      )
    )
  }

  fun getMatchesForDate(targetDate: LocalDate = LocalDate.now()): List<MatchDto> {
    val dateStr = targetDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US))
    val isToday = targetDate == LocalDate.now()
    val isPast = targetDate.isBefore(LocalDate.now())

    val matches = mutableListOf<MatchDto>()

    if (isToday) {
      matches.addAll(getLiveMatches())
    }

    // Premier league fixtures
    matches.add(
      MatchDto(
        id = 201,
        status = if (isPast) "finished" else if (isToday) "finished" else "scheduled",
        matchMinute = if (isPast || isToday) "FT" else null,
        homeScore = if (isPast || isToday) 3 else null,
        awayScore = if (isPast || isToday) 1 else null,
        matchTime = "$dateStr 12:30:00",
        leagueId = 1,
        leagueName = "Premier League",
        country = "England",
        homeId = 9,
        homeName = "Manchester City",
        homeLogo = null,
        awayId = 10,
        awayName = "Liverpool",
        awayLogo = null
      )
    )
    matches.add(
      MatchDto(
        id = 202,
        status = if (isPast) "finished" else "scheduled",
        matchMinute = if (isPast) "FT" else null,
        homeScore = if (isPast) 1 else null,
        awayScore = if (isPast) 2 else null,
        matchTime = "$dateStr 15:00:00",
        leagueId = 1,
        leagueName = "Premier League",
        country = "England",
        homeId = 11,
        homeName = "Tottenham",
        homeLogo = null,
        awayId = 12,
        awayName = "Newcastle United",
        awayLogo = null
      )
    )

    // Champions League / La Liga
    matches.add(
      MatchDto(
        id = 203,
        status = if (isPast) "finished" else "scheduled",
        matchMinute = if (isPast) "FT" else null,
        homeScore = if (isPast) 2 else null,
        awayScore = if (isPast) 0 else null,
        matchTime = "$dateStr 17:30:00",
        leagueId = 3,
        leagueName = "La Liga",
        country = "Spain",
        homeId = 13,
        homeName = "Real Sociedad",
        homeLogo = null,
        awayId = 14,
        awayName = "Sevilla",
        awayLogo = null
      )
    )
    matches.add(
      MatchDto(
        id = 204,
        status = if (isPast) "finished" else "scheduled",
        matchMinute = if (isPast) "FT" else null,
        homeScore = if (isPast) 4 else null,
        awayScore = if (isPast) 1 else null,
        matchTime = "$dateStr 20:00:00",
        leagueId = 2,
        leagueName = "UEFA Champions League",
        country = "Europe",
        homeId = 15,
        homeName = "Paris Saint-Germain",
        homeLogo = null,
        awayId = 16,
        awayName = "AC Milan",
        awayLogo = null
      )
    )

    // Bundesliga & Serie A
    matches.add(
      MatchDto(
        id = 205,
        status = if (isPast) "finished" else "scheduled",
        matchMinute = if (isPast) "FT" else null,
        homeScore = if (isPast) 2 else null,
        awayScore = if (isPast) 2 else null,
        matchTime = "$dateStr 18:30:00",
        leagueId = 5,
        leagueName = "Bundesliga",
        country = "Germany",
        homeId = 17,
        homeName = "Borussia Dortmund",
        homeLogo = null,
        awayId = 18,
        awayName = "Bayer Leverkusen",
        awayLogo = null
      )
    )
    matches.add(
      MatchDto(
        id = 206,
        status = if (isPast) "finished" else "scheduled",
        matchMinute = if (isPast) "FT" else null,
        homeScore = if (isPast) 0 else null,
        awayScore = if (isPast) 1 else null,
        matchTime = "$dateStr 19:45:00",
        leagueId = 4,
        leagueName = "Serie A",
        country = "Italy",
        homeId = 19,
        homeName = "Napoli",
        homeLogo = null,
        awayId = 20,
        awayName = "AS Roma",
        awayLogo = null
      )
    )

    return matches
  }

  fun getRemoteTranslations(langCode: String): Map<String, String> {
    return when (langCode.lowercase()) {
      "es" -> mapOf(
        "tab_live" to "En Vivo",
        "tab_matches" to "Partidos",
        "tab_leagues" to "Ligas",
        "tab_settings" to "Ajustes",
        "live_matches" to "Partidos en Directo",
        "status_live" to "EN VIVO"
      )
      "de" -> mapOf(
        "tab_live" to "Live",
        "tab_matches" to "Spiele",
        "tab_leagues" to "Ligen",
        "tab_settings" to "Einstellungen",
        "live_matches" to "Live-Spiele",
        "status_live" to "LIVE"
      )
      "fr" -> mapOf(
        "tab_live" to "En Direct",
        "tab_matches" to "Matchs",
        "tab_leagues" to "Ligues",
        "tab_settings" to "Paramètres",
        "live_matches" to "Matchs en Direct",
        "status_live" to "DIRECT"
      )
      "ar" -> mapOf(
        "tab_live" to "مباشر",
        "tab_matches" to "المباريات",
        "tab_leagues" to "الدوريات",
        "tab_settings" to "الإعدادات",
        "live_matches" to "المباريات المباشرة",
        "status_live" to "مباشر"
      )
      else -> emptyMap()
    }
  }
}
