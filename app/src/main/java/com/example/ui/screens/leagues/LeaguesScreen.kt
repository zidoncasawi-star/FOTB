package com.example.ui.screens.leagues

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.TranslationManager
import com.example.ui.common.EmptyState
import com.example.ui.common.ErrorState
import com.example.ui.common.TeamLogo
import com.example.ui.theme.AccentGold
import com.example.ui.theme.LiveGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaguesScreen(
  viewModel: LeaguesViewModel,
  translationManager: TranslationManager,
  onLeagueSelected: (Int, String) -> Unit,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val focusManager = LocalFocusManager.current

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .width(4.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(LiveGreen)
            )

            Text(
              text = translationManager.getString(R.string.tab_leagues, "tab_leagues"),
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Black,
              color = MaterialTheme.colorScheme.onBackground
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { viewModel.updateSearchQuery(it) },
        placeholder = {
          Text(
            text = translationManager.getString(R.string.search_leagues, "search_leagues"),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
        },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Clear search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface,
          focusedBorderColor = LiveGreen,
          unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 8.dp)
          .testTag("search_leagues_input")
      )

      // Content Area
      Box(
        modifier = Modifier
          .fillMaxSize()
          .weight(1f)
      ) {
        when (val state = uiState) {
          is LeaguesUiState.Loading -> {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              CircularProgressIndicator(
                color = LiveGreen,
                strokeWidth = 3.dp
              )
            }
          }
          is LeaguesUiState.Error -> {
            ErrorState(
              title = translationManager.getString(R.string.error_loading, "error_loading"),
              description = state.message,
              retryLabel = translationManager.getString(R.string.retry, "retry"),
              onRetry = { viewModel.loadLeagues() }
            )
          }
          is LeaguesUiState.Success -> {
            if (state.countryGroups.isEmpty()) {
              EmptyState(
                title = translationManager.getString(R.string.no_leagues_found, "no_leagues_found"),
                description = "Try searching for a different competition or country name."
              )
            } else {
              LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
              ) {
                state.countryGroups.forEach { countryGroup ->
                  item(key = "country_${countryGroup.country}") {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(8.dp),
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                      )
                      Text(
                        text = countryGroup.country.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                      )
                    }
                  }

                  items(countryGroup.leagues, key = { it.id }) { league ->
                    Box(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                          1.dp,
                          MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                          RoundedCornerShape(10.dp)
                        )
                        .clickable { onLeagueSelected(league.id, league.name) }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("league_item_${league.id}")
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                      ) {
                        Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(12.dp),
                          modifier = Modifier.weight(1f)
                        ) {
                          TeamLogo(
                            name = league.name,
                            logoUrl = league.logoUrl,
                            size = 32.dp
                          )

                          Column {
                            Text(
                              text = league.name,
                              style = MaterialTheme.typography.titleSmall,
                              fontWeight = FontWeight.Bold,
                              color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                              text = league.country,
                              style = MaterialTheme.typography.bodySmall,
                              color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                          }
                        }

                        Icon(
                          imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                          contentDescription = "Select league",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(20.dp)
                        )
                      }
                    }
                  }
                }

                item(key = "bottom_space") {
                  Spacer(modifier = Modifier.height(16.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}
