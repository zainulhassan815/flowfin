package com.flowfin.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.theme.FlowFinTheme

private val SheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)

/**
 * Thin FlowFin wrapper over M3's [ModalBottomSheet]. M3 owns all the mechanics
 * — scrim, drag-to-dismiss, sheet state, insets, predictive back — while this
 * bakes in the FlowFin shape, surface color, drag handle, and the experimental
 * opt-in so screens don't repeat them. Compose the sheet body in [content],
 * typically a [FlowFinSheetHeader] followed by the picker rows.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlowFinModalBottomSheet(
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  sheetState: SheetState = rememberModalBottomSheetState(),
  content: @Composable ColumnScope.() -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismissRequest,
    modifier = modifier,
    sheetState = sheetState,
    shape = SheetShape,
    containerColor = FlowFinTheme.colors.surface2,
    dragHandle = { FlowFinDragHandle() },
    content = content,
  )
}

/**
 * The header for a picker sheet: a circular close button on the left, an
 * italic-serif title centered against the full width, and an optional ghost
 * action (e.g. "Done") on the right, over a hairline bottom divider.
 */
@Composable
fun FlowFinSheetHeader(
  title: String,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
  actionLabel: String? = null,
  onAction: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors

  Box(
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
      .padding(start = 16.dp, top = 10.dp, end = 16.dp, bottom = 14.dp),
  ) {
    CloseButton(onClick = onClose, modifier = Modifier.align(Alignment.CenterStart))

    Text(
      text = title,
      modifier = Modifier.align(Alignment.Center),
      style = FlowFinTheme.typography.h2.copy(fontSize = 16.sp),
      color = palette.text,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    if (actionLabel != null) {
      Text(
        text = actionLabel.uppercase(),
        modifier = Modifier
          .align(Alignment.CenterEnd)
          .clickable(onClick = onAction)
          .padding(horizontal = 4.dp, vertical = 8.dp),
        style = FlowFinTheme.typography.caption.copy(fontWeight = FontWeight.SemiBold),
        color = palette.accent,
      )
    }
  }
}

@Composable
private fun FlowFinDragHandle() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 10.dp, bottom = 6.dp),
    contentAlignment = Alignment.Center,
  ) {
    Box(
      modifier = Modifier
        .size(width = 36.dp, height = 4.dp)
        .clip(RoundedCornerShape(2.dp))
        .background(Color.White.copy(alpha = 0.18f)),
    )
  }
}

@Composable
private fun CloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
  val palette = FlowFinTheme.colors
  Box(
    modifier = modifier
      .size(28.dp)
      .clip(CircleShape)
      .border(1.dp, palette.borderStrong, CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center,
  ) {
    Icon(
      imageVector = Icons.Rounded.Close,
      contentDescription = "Close",
      tint = palette.textMute,
      modifier = Modifier.size(14.dp),
    )
  }
}

// The live ModalBottomSheet renders in its own window and doesn't show in the
// preview pane, so this mocks the sheet surface to show the composed anatomy.
@Preview(name = "Bottom sheet", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 360)
@Composable
private fun PreviewBottomSheet() = FlowFinTheme {
  val palette = FlowFinTheme.colors
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .clip(SheetShape)
      .background(palette.surface2),
  ) {
    FlowFinDragHandle()
    FlowFinSheetHeader(title = "Select account", onClose = {})
    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
      FlowFinPickerRow(
        icon = Icons.Rounded.AccountBalance,
        name = "Bank",
        meta = "HBL · Primary",
        amount = "40,000",
        tint = palette.categories.bank,
        selected = true,
        onClick = {},
      )
      FlowFinPickerRow(
        icon = Icons.Rounded.AccountBalanceWallet,
        name = "Cash",
        meta = "Wallet",
        amount = "7,000",
        tint = palette.categories.cash,
        selected = false,
        onClick = {},
      )
    }
  }
}
