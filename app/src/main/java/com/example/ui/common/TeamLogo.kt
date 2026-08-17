package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun TeamLogo(
  name: String,
  logoUrl: String?,
  size: Dp = 32.dp,
  modifier: Modifier = Modifier
) {
  val initials = name.trim().split(" ")
    .take(2)
    .mapNotNull { it.firstOrNull()?.uppercase() }
    .joinToString("")
    .take(2)
    .ifEmpty { "FT" }

  // Consistent background color derived from team name
  val colorHash = kotlin.math.abs(name.hashCode())
  val baseColors = listOf(
    Color(0xFFE11D48),
    Color(0xFF2563EB),
    Color(0xFF059669),
    Color(0xFFD97706),
    Color(0xFF7C3AED),
    Color(0xFF0891B2),
    Color(0xFF475569)
  )
  val badgeBg = baseColors[colorHash % baseColors.size]

  Box(
    modifier = modifier
      .size(size)
      .clip(CircleShape)
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .border(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    if (!logoUrl.isNullOrBlank()) {
      AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
          .data(logoUrl)
          .crossfade(true)
          .build(),
        contentDescription = name,
        contentScale = ContentScale.Crop,
        modifier = Modifier
          .size(size)
          .clip(CircleShape)
      )
    } else {
      Box(
        modifier = Modifier
          .size(size)
          .clip(CircleShape)
          .background(badgeBg.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = initials,
          color = badgeBg,
          fontSize = (size.value * 0.38f).sp,
          fontWeight = FontWeight.Black
        )
      }
    }
  }
}
