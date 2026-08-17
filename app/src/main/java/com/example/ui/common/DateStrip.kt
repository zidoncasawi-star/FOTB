package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LiveGreen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateStrip(
  selectedDate: LocalDate,
  onDateSelected: (LocalDate) -> Unit,
  todayLabel: String = "Today",
  yesterdayLabel: String = "Yesterday",
  tomorrowLabel: String = "Tomorrow",
  modifier: Modifier = Modifier
) {
  val today = remember { LocalDate.now() }
  // 7 days before to 7 days after
  val dateRange = remember {
    (-7L..7L).map { offset -> today.plusDays(offset) }
  }

  val listState = rememberLazyListState()

  // Scroll to selected date on initial composition
  LaunchedEffect(selectedDate) {
    val index = dateRange.indexOf(selectedDate)
    if (index >= 0) {
      listState.animateScrollToItem(maxOf(0, index - 2))
    }
  }

  LazyRow(
    state = listState,
    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = modifier
      .fillMaxWidth()
      .background(MaterialTheme.colorScheme.background)
  ) {
    items(dateRange, key = { it.toString() }) { date ->
      val isSelected = date == selectedDate
      val isToday = date == today
      val isYesterday = date == today.minusDays(1)
      val isTomorrow = date == today.plusDays(1)

      val subtitle = when {
        isToday -> todayLabel
        isYesterday -> yesterdayLabel
        isTomorrow -> tomorrowLabel
        else -> date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
      }

      val dayNum = date.dayOfMonth.toString()
      val monthStr = date.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))

      Box(
        modifier = Modifier
          .width(68.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(
            if (isSelected) LiveGreen
            else MaterialTheme.colorScheme.surface
          )
          .then(
            if (!isSelected && isToday) {
              Modifier.border(1.dp, LiveGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            } else if (!isSelected) {
              Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
            } else {
              Modifier
            }
          )
          .clickable { onDateSelected(date) }
          .padding(vertical = 8.dp, horizontal = 6.dp)
          .testTag("date_item_${date}"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Text(
            text = subtitle.uppercase(),
            fontSize = 10.sp,
            fontWeight = if (isSelected || isToday) FontWeight.Black else FontWeight.Bold,
            color = when {
              isSelected -> MaterialTheme.colorScheme.onPrimary
              isToday -> LiveGreen
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
          )

          Text(
            text = dayNum,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface
          )

          Text(
            text = monthStr.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
          )
        }
      }
    }
  }
}
