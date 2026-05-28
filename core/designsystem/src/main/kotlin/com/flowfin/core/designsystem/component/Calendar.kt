package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn

private val WeekdayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

/**
 * Month calendar for date pickers. Shows the month containing [month] (its day
 * is ignored) as a Sunday-first grid: today carries an accent dot, the
 * [selectedDate] fills with the cream accent. Stateless — month navigation and
 * day taps are reported through [onPreviousMonth] / [onNextMonth] /
 * [onSelectDate], and the caller owns which month and date are shown.
 *
 * [today] is passed in rather than read from the clock so the component stays
 * pure and previewable; leave it null on screens where the dot isn't wanted.
 */
@Composable
fun FlowFinCalendar(
  month: LocalDate,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  onSelectDate: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
  selectedDate: LocalDate? = null,
  today: LocalDate? = null,
) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(14.dp)

  Column(
    modifier = modifier
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .padding(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 16.dp),
  ) {
    CalendarNav(month = month, onPrevious = onPreviousMonth, onNext = onNextMonth)

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 12.dp, bottom = 6.dp),
      horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
      WeekdayLabels.forEach { label ->
        Text(
          text = label,
          modifier = Modifier.weight(1f),
          textAlign = TextAlign.Center,
          style = FlowFinTheme.typography.caption.copy(
            fontSize = 9.sp,
            letterSpacing = 0.1.em,
          ),
          color = palette.textSoft,
        )
      }
    }

    val weeks = remember(month.year, month.monthNumber) { monthWeeks(month) }
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
      weeks.forEach { week ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
          week.forEach { day ->
            DayCell(
              day = day,
              month = month,
              selectedDate = selectedDate,
              today = today,
              onSelectDate = onSelectDate,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CalendarNav(month: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    CalArrow(icon = Icons.Rounded.ChevronLeft, contentDescription = "Previous month", onClick = onPrevious)
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = monthName(month),
        modifier = Modifier.alignByBaseline(),
        style = FlowFinTheme.typography.h2.copy(fontSize = 18.sp),
        color = palette.text,
      )
      Text(
        text = month.year.toString(),
        modifier = Modifier
          .alignByBaseline()
          .padding(start = 6.dp),
        style = FlowFinTheme.typography.monoNum.copy(
          fontSize = 12.sp,
          fontWeight = FontWeight.Normal,
          letterSpacing = 0.em,
        ),
        color = palette.textMute,
      )
    }
    CalArrow(icon = Icons.Rounded.ChevronRight, contentDescription = "Next month", onClick = onNext)
  }
}

@Composable
private fun CalArrow(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  Box(
    modifier = Modifier
      .size(28.dp)
      .clip(CircleShape)
      .border(1.dp, palette.borderStrong, CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = palette.textMute,
      modifier = Modifier.size(12.dp),
    )
  }
}

@Composable
private fun DayCell(
  day: Int?,
  month: LocalDate,
  selectedDate: LocalDate?,
  today: LocalDate?,
  onSelectDate: (LocalDate) -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  val date = day?.let { LocalDate(month.year, month.monthNumber, it) }
  val isSelected = date != null && date == selectedDate
  val isToday = date != null && date == today

  val textColor = when {
    isSelected -> palette.bg
    isToday -> palette.accent
    else -> palette.text
  }

  Box(
    modifier = modifier
      .aspectRatio(1f)
      .clip(CircleShape)
      .background(if (isSelected) palette.accent else Color.Transparent)
      .then(if (date != null) Modifier.clickable { onSelectDate(date) } else Modifier),
    contentAlignment = Alignment.Center,
  ) {
    if (day != null) {
      Text(
        text = day.toString(),
        style = FlowFinTheme.typography.monoNum.copy(
          fontSize = 13.sp,
          fontWeight = if (isSelected || isToday) FontWeight.Medium else FontWeight.Normal,
        ),
        color = textColor,
      )
      if (isToday && !isSelected) {
        Box(
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 4.dp)
            .size(3.dp)
            .clip(CircleShape)
            .background(palette.accent),
        )
      }
    }
  }
}

private fun monthName(date: LocalDate): String =
  date.month.name.lowercase().replaceFirstChar { it.uppercase() }

private fun monthWeeks(month: LocalDate): List<List<Int?>> {
  val first = LocalDate(month.year, month.monthNumber, 1)
  val leading = first.dayOfWeek.isoDayNumber % 7
  val daysInMonth = first.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY).dayOfMonth

  val cells = buildList {
    repeat(leading) { add(null) }
    for (day in 1..daysInMonth) add(day)
    while (size % 7 != 0) add(null)
  }
  return cells.chunked(7)
}

/**
 * The month navigation strip on the Reports screen: a centered italic month
 * name over its mono year, flanked by prev/next buttons. Like the calendar it
 * is stateless — [month] (day ignored) drives the label and stepping is
 * reported through [onPreviousMonth] / [onNextMonth].
 */
@Composable
fun FlowFinMonthStrip(
  month: LocalDate,
  onPreviousMonth: () -> Unit,
  onNextMonth: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(start = 4.dp, top = 8.dp, end = 4.dp, bottom = 18.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    MonthNavButton(icon = Icons.Rounded.ChevronLeft, contentDescription = "Previous month", onClick = onPreviousMonth)
    Column(
      modifier = Modifier.weight(1f),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = monthName(month),
        style = FlowFinTheme.typography.h2,
        color = palette.text,
      )
      Text(
        text = month.year.toString(),
        modifier = Modifier.padding(top = 2.dp),
        style = FlowFinTheme.typography.caption.copy(
          fontWeight = FontWeight.Normal,
          letterSpacing = 0.22.em,
        ),
        color = palette.textSoft,
      )
    }
    MonthNavButton(icon = Icons.Rounded.ChevronRight, contentDescription = "Next month", onClick = onNextMonth)
  }
}

@Composable
private fun MonthNavButton(icon: ImageVector, contentDescription: String, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  val shape = RoundedCornerShape(10.dp)
  Box(
    modifier = Modifier
      .size(36.dp)
      .clip(shape)
      .background(palette.surface)
      .border(1.dp, palette.border, shape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = palette.textMute,
      modifier = Modifier.size(14.dp),
    )
  }
}

@Preview(name = "Calendar", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewCalendar() = FlowFinTheme {
  val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
  var month by remember { mutableStateOf(today) }
  var selected by remember { mutableStateOf<LocalDate?>(LocalDate(today.year, today.monthNumber, 27)) }

  Box(modifier = Modifier.padding(24.dp)) {
    FlowFinCalendar(
      month = month,
      onPreviousMonth = { month = month.minus(1, DateTimeUnit.MONTH) },
      onNextMonth = { month = month.plus(1, DateTimeUnit.MONTH) },
      onSelectDate = { selected = it },
      selectedDate = selected,
      today = today,
      modifier = Modifier.width(320.dp),
    )
  }
}

@Preview(name = "Month strip", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewMonthStrip() = FlowFinTheme {
  var month by remember { mutableStateOf(LocalDate(2026, 12, 1)) }

  Box(modifier = Modifier.padding(24.dp)) {
    FlowFinMonthStrip(
      month = month,
      onPreviousMonth = { month = month.minus(1, DateTimeUnit.MONTH) },
      onNextMonth = { month = month.plus(1, DateTimeUnit.MONTH) },
      modifier = Modifier.width(320.dp),
    )
  }
}
