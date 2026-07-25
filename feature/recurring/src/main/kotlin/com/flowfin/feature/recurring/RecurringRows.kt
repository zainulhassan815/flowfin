package com.flowfin.feature.recurring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.RecurringScheduleId
import com.flowfin.core.resources.R
import com.flowfin.core.ui.UiText
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryIcon

/** Row pieces shared by the tab's own paused section and the "view paused" screen. */

@Composable
internal fun PausedRow(
  row: RecurringPausedUi,
  onResume: (RecurringScheduleId) -> Unit,
  modifier: Modifier = Modifier,
  onClick: (RecurringScheduleId) -> Unit = {},
) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = modifier.fillMaxWidth().clickable { onClick(row.id) }.padding(vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    // Muted, not category-tinted — the desaturated look reads as "not running".
    Box(
      modifier = Modifier
        .size(38.dp)
        .background(palette.surface2, RoundedCornerShape(10.dp))
        .border(1.dp, palette.borderStrong, RoundedCornerShape(10.dp)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = categoryIcon(row.iconKey),
        contentDescription = null,
        modifier = Modifier.size(17.dp).alpha(0.7f),
        tint = palette.textSoft,
      )
    }
    Spacer(Modifier.width(14.dp))
    Column(Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(row.name, style = FlowFinTheme.typography.body.copy(fontSize = 16.sp), color = palette.textMute, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(8.dp))
        FreqTag(row.freq)
      }
      Text(
        text = row.pausedSince.asString(),
        modifier = Modifier.padding(top = 4.dp),
        style = FlowFinTheme.typography.caption,
        color = palette.textSoft,
      )
    }
    Amount(row.amountWhole, row.amountDecimal)
    Spacer(Modifier.width(10.dp))
    Box(
      modifier = Modifier
        .size(34.dp)
        .border(1.dp, palette.borderStrong, CircleShape)
        .clickable(onClickLabel = stringResource(R.string.recurring_resume_action, row.name)) { onResume(row.id) },
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = FlowFinIcons.Play,
        contentDescription = stringResource(R.string.recurring_resume_action, row.name),
        modifier = Modifier.size(13.dp),
        tint = palette.textMute,
      )
    }
  }
}

@Composable
internal fun FreqTag(freq: UiText) {
  val palette = FlowFinTheme.colors
  Text(
    text = freq.asString().uppercase(),
    modifier = Modifier
      .border(1.dp, palette.border, RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp),
    style = FlowFinTheme.typography.caption.copy(fontSize = 8.5.sp),
    color = palette.textSoft,
  )
}

@Composable
internal fun Amount(whole: String, decimal: String) {
  val palette = FlowFinTheme.colors
  Row(verticalAlignment = Alignment.Bottom) {
    Text(whole, style = FlowFinTheme.typography.monoNum.copy(fontSize = 15.sp), color = palette.textMute)
    Text(decimal, style = FlowFinTheme.typography.monoNum.copy(fontSize = 12.sp), color = palette.textFaint)
  }
}

@Composable
internal fun IconTile(image: ImageVector, tint: Color) {
  Box(
    modifier = Modifier
      .size(38.dp)
      .background(tint.copy(alpha = 0.10f), RoundedCornerShape(10.dp))
      .border(1.dp, tint.copy(alpha = 0.26f), RoundedCornerShape(10.dp)),
    contentAlignment = Alignment.Center,
  ) {
    Icon(imageVector = image, contentDescription = null, modifier = Modifier.size(18.dp), tint = tint)
  }
}
