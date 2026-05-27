package com.flowfin.core.designsystem.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.flowfin.core.designsystem.theme.FlowFinTheme

/**
 * Storybook-style previews for the foundation tokens. One `@Preview` per
 * concern; future components add their own preview files alongside this one.
 */

private data class Swatch(val token: String, val color: Color, val hex: String)

@Preview(name = "Surfaces", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 380)
@Composable
private fun PreviewSurfaces() = TokenPanel("Surfaces") {
  with(FlowFinTheme.colors) {
    listOf(
      Swatch("bg", bg, "#08080A"),
      Swatch("surface", surface, "#101013"),
      Swatch("surface2", surface2, "#16161A"),
      Swatch("surface3", surface3, "#1C1C20"),
      Swatch("border", border, "#1E1E22"),
      Swatch("borderStrong", borderStrong, "#2A2A2F"),
    )
  }
}

@Preview(name = "Text", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 380)
@Composable
private fun PreviewText() = TokenPanel("Text") {
  with(FlowFinTheme.colors) {
    listOf(
      Swatch("text", text, "#F2F2F4"),
      Swatch("textMute", textMute, "#B4B4BC"),
      Swatch("textSoft", textSoft, "#8A8A92"),
      Swatch("textFaint", textFaint, "#565660"),
    )
  }
}

@Preview(name = "Semantic", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 380)
@Composable
private fun PreviewSemantic() = TokenPanel("Semantic") {
  with(FlowFinTheme.colors) {
    listOf(
      Swatch("accent", accent, "#E8DCC0"),
      Swatch("positive", positive, "#9CD4A2"),
      Swatch("warning", warning, "#E8B66E"),
      Swatch("negative", negative, "#E08A8A"),
      Swatch("transfer", transfer, "#82C5D4"),
    )
  }
}

@Preview(name = "Categories", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 380, heightDp = 720)
@Composable
private fun PreviewCategories() = TokenPanel("Categories") {
  with(FlowFinTheme.colors.categories) {
    listOf(
      Swatch("bank", bank, "#82C5D4"),
      Swatch("cash", cash, "#C5D982"),
      Swatch("food", food, "#E8A87B"),
      Swatch("grocery", grocery, "#A8D479"),
      Swatch("transport", transport, "#8AB4E0"),
      Swatch("fun", fun_, "#C98ED4"),
      Swatch("salary", salary, "#9CD4A2"),
      Swatch("subs", subs, "#9A8AE0"),
      Swatch("rent", rent, "#D89A82"),
      Swatch("utilities", utilities, "#E8CC7B"),
      Swatch("shop", shop, "#E89AB8"),
      Swatch("health", health, "#DC8A8A"),
      Swatch("edu", edu, "#8AC9D4"),
      Swatch("care", care, "#C9A8D4"),
      Swatch("debt", debt, "#E08A8A"),
      Swatch("other", other, "#B4B4BC"),
    )
  }
}

@Preview(name = "Avatar tints", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 380)
@Composable
private fun PreviewAvatars() = TokenPanel("Avatar tints") {
  with(FlowFinTheme.colors.avatars) {
    listOf(
      Swatch("avatar 1", tint1, "#82C5D4"),
      Swatch("avatar 2", tint2, "#C98ED4"),
      Swatch("avatar 3", tint3, "#E8A87B"),
      Swatch("avatar 4", tint4, "#9CD4A2"),
      Swatch("avatar 5", tint5, "#E89AB8"),
    )
  }
}

@Preview(name = "Type scale", backgroundColor = 0xFF08080A, showBackground = true, widthDp = 420, heightDp = 1100)
@Composable
private fun PreviewTypeScale() = FlowFinTheme {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(FlowFinTheme.colors.bg)
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(18.dp),
  ) {
    val type = FlowFinTheme.typography
    TypeRow("Hero amount", "Geist Mono · 56 / 300 · -0.04em", type.hero, "40,000")
    TypeRow("Display", "Literata · 48 italic · -0.03em", type.display, "Welcome back")
    TypeRow("H1 — Page", "Literata · 28 italic · -0.02em", type.h1, "Accounts")
    TypeRow("H2 — Section", "Literata · 22 italic · -0.02em", type.h2, "Recent activity")
    TypeRow("Body large", "Literata · 17 / 400", type.bodyLg, "Allocated to Food budget.")
    TypeRow("Body", "Literata · 15 / 400", type.body, "Five payments due this week.")
    TypeRow("Label", "Geist Mono · 11 · 0.22em", type.label, "TOTAL BALANCE")
    TypeRow("Caption", "Geist Mono · 10 · 0.18em", type.caption, "DUE THURSDAY")
    TypeRow("Mono number", "Geist Mono · 22 / 500", type.monoNum, "12,450")
  }
}

@Composable
private fun TokenPanel(title: String, swatches: @Composable () -> List<Swatch>) = FlowFinTheme {
  val resolved = swatches()
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(FlowFinTheme.colors.bg)
      .padding(20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    Text(
      text = title.uppercase(),
      style = FlowFinTheme.typography.caption,
      color = FlowFinTheme.colors.textSoft,
    )
    LazyVerticalGrid(
      columns = GridCells.Adaptive(minSize = 110.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(0.dp),
    ) {
      items(resolved) { SwatchCard(it) }
    }
  }
}

@Composable
private fun SwatchCard(swatch: Swatch) {
  Column(
    modifier = Modifier
      .clip(RoundedCornerShape(12.dp))
      .background(FlowFinTheme.colors.surface)
      .border(1.dp, FlowFinTheme.colors.border, RoundedCornerShape(12.dp)),
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .background(swatch.color),
    )
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Text(
        text = swatch.token,
        style = FlowFinTheme.typography.body,
        color = FlowFinTheme.colors.text,
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = swatch.hex,
        style = FlowFinTheme.typography.caption,
        color = FlowFinTheme.colors.textSoft,
      )
    }
  }
}

@Composable
private fun TypeRow(name: String, spec: String, style: TextStyle, sample: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Column(modifier = Modifier.width(160.dp)) {
      Text(
        text = name,
        style = FlowFinTheme.typography.label,
        color = FlowFinTheme.colors.text,
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = spec,
        style = FlowFinTheme.typography.caption,
        color = FlowFinTheme.colors.textSoft,
      )
    }
    Spacer(Modifier.width(20.dp))
    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
      Text(text = sample, style = style, color = FlowFinTheme.colors.text)
    }
  }
}
