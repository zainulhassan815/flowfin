package com.flowfin.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * The header on detail/add screens: an optional circular back button pinned to
 * the start, an italic-serif title centered against the full width, and an
 * optional ghost action pinned to the end.
 *
 * Centering follows M3's centered app bar: the title is constrained to the
 * space the back button and action leave free — so a long title ellipsizes
 * rather than overlapping — then centered on the full width and nudged inward
 * only if it would still collide. The title therefore stays put regardless of
 * whether either side is present or how wide the action label is.
 *
 * [actionTint] colors the action label so it can adopt the screen's semantic
 * accent (cream to save, negative to delete, …). The action shows only when
 * [actionLabel] is set; the back button only when [onBack] is provided.
 */
@Composable
fun FlowFinPageHeader(
  title: String,
  modifier: Modifier = Modifier,
  onBack: (() -> Unit)? = null,
  actionLabel: String? = null,
  actionTint: Color = FlowFinTheme.colors.accent,
  onAction: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors

  Layout(
    modifier = modifier
      .fillMaxWidth()
      .drawBehind {
        val stroke = 1.dp.toPx()
        drawRect(
          color = palette.border,
          topLeft = Offset(0f, size.height - stroke),
          size = Size(size.width, stroke),
        )
      }
      .padding(horizontal = 18.dp, vertical = 14.dp),
    content = {
      if (onBack != null) {
        FlowFinOutlinedIconButton(
          onClick = onBack,
          icon = Icons.Rounded.ChevronLeft,
          contentDescription = "Back",
        )
      } else {
        Box(Modifier)
      }

      Text(
        text = title,
        style = FlowFinTheme.typography.h2.copy(
          fontSize = 18.sp,
          letterSpacing = (-0.01).em,
        ),
        color = palette.text,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )

      if (actionLabel != null) {
        Text(
          text = actionLabel.uppercase(),
          modifier = Modifier
            .clickable(onClick = onAction)
            .padding(horizontal = 4.dp, vertical = 8.dp),
          style = FlowFinTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
          color = actionTint,
        )
      } else {
        Box(Modifier)
      }
    },
  ) { measurables, constraints ->
    val loose = constraints.copy(minWidth = 0, minHeight = 0)
    val back = measurables[0].measure(loose)
    val action = measurables[2].measure(loose)

    val width = constraints.maxWidth
    val maxTitleWidth = (width - back.width - action.width).coerceAtLeast(0)
    val titleText = measurables[1].measure(loose.copy(maxWidth = maxTitleWidth))

    val height = maxOf(back.height, titleText.height, action.height)

    var titleX = (width - titleText.width) / 2
    if (titleX < back.width) {
      titleX = back.width
    } else if (titleX + titleText.width > width - action.width) {
      titleX = width - action.width - titleText.width
    }

    layout(width, height) {
      back.place(0, (height - back.height) / 2)
      titleText.place(titleX, (height - titleText.height) / 2)
      action.place(width - action.width, (height - action.height) / 2)
    }
  }
}

@Preview(name = "Page header", backgroundColor = 0xFF08080A, showBackground = true)
@Composable
private fun PreviewPageHeader() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
    FlowFinPageHeader(
      title = "New transaction",
      onBack = {},
      actionLabel = "Save",
    )
    FlowFinPageHeader(
      title = "Edit account",
      onBack = {},
      actionLabel = "Delete",
      actionTint = palette.negative,
    )
    FlowFinPageHeader(title = "Settings", onBack = {})
    FlowFinPageHeader(
      title = "A rather long screen title that must clip",
      onBack = {},
      actionLabel = "Save",
    )
  }
}
