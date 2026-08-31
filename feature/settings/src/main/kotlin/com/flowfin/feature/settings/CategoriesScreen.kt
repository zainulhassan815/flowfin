@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.flowfin.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flowfin.core.designsystem.component.FlowFinButton
import com.flowfin.core.designsystem.component.FlowFinModalBottomSheet
import com.flowfin.core.designsystem.component.FlowFinOutlinedButton
import com.flowfin.core.designsystem.component.FlowFinPageHeader
import com.flowfin.core.designsystem.component.FlowFinScopeTabs
import com.flowfin.core.designsystem.component.FlowFinScreenScaffold
import com.flowfin.core.designsystem.component.FlowFinSheetHeader
import com.flowfin.core.designsystem.component.FlowFinTextField
import com.flowfin.core.designsystem.component.FlowFinTileIcon
import com.flowfin.core.designsystem.icon.FlowFinIcons
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.model.CategoryId
import com.flowfin.core.model.CategoryScope
import com.flowfin.core.resources.R
import com.flowfin.core.ui.asString
import com.flowfin.core.ui.categoryColor
import com.flowfin.core.ui.categoryIcon

private val HORIZONTAL = 24.dp

/** Gap between a picker cell's ring and its content. */
private val CELL_PADDING = 5.dp

/** The corner radius FlowFinTileIcon uses at the size the icon grid draws it. */
private val TILE_SIZE_RADIUS = 10.dp

@Composable
fun CategoriesScreen(
  state: CategoriesUiState,
  snackbarHostState: SnackbarHostState,
  modifier: Modifier = Modifier,
  onBack: () -> Unit = {},
  onSelectScope: (CategoryScope) -> Unit = {},
  onAdd: () -> Unit = {},
  onEdit: (CategoryId) -> Unit = {},
  onToggleArchived: () -> Unit = {},
  onDismissSheet: () -> Unit = {},
  onNameChange: (String) -> Unit = {},
  onIconChange: (String) -> Unit = {},
  onColorChange: (String) -> Unit = {},
  onArchive: (CategoryId) -> Unit = {},
  onUnarchive: (CategoryId) -> Unit = {},
  onSave: () -> Unit = {},
) {
  val palette = FlowFinTheme.colors

  FlowFinScreenScaffold(
    modifier = modifier,
    topBar = {
      FlowFinPageHeader(
        title = stringResource(R.string.categories_title),
        onBack = onBack,
        backContentDescription = stringResource(R.string.action_back),
        actionLabel = stringResource(R.string.categories_add),
        onAction = onAdd,
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) },
  ) {
    FlowFinScopeTabs(
      options = listOf(CategoryScope.EXPENSE, CategoryScope.INCOME),
      selected = state.scope,
      onSelect = onSelectScope,
      label = {
        stringResource(
          if (it == CategoryScope.EXPENSE) R.string.add_tx_type_expense else R.string.add_tx_type_income,
        )
      },
      modifier = Modifier.padding(horizontal = HORIZONTAL, vertical = 8.dp),
    )

    // No bottomBar, so the list clears the gesture bar itself.
    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
      contentPadding = PaddingValues(
        start = HORIZONTAL,
        end = HORIZONTAL,
        top = 4.dp,
        bottom = 24.dp + navBarBottom,
      ),
    ) {
      item(key = "summary") { Summary(state.summary) }

      if (state.defaults.isNotEmpty()) {
        item(key = "h-default") { SectionLabel(stringResource(R.string.categories_default), state.defaults.size) }
        items(state.defaults, key = { it.id.value.toString() }) { CategoryRow(it, onEdit) }
      }

      item(key = "h-custom") { SectionLabel(stringResource(R.string.categories_custom), state.customs.size) }
      if (state.customs.isEmpty()) {
        item(key = "custom-empty") {
          Text(
            text = stringResource(R.string.categories_custom_empty),
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            style = FlowFinTheme.typography.caption,
            color = palette.textSoft,
          )
        }
      } else {
        items(state.customs, key = { it.id.value.toString() }) { CategoryRow(it, onEdit) }
      }

      item(key = "add") {
        FlowFinOutlinedButton(
          onClick = onAdd,
          text = stringResource(R.string.categories_add),
          modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
          leadingIcon = FlowFinIcons.Add,
        )
      }

      if (state.archived.isNotEmpty()) {
        item(key = "archived-toggle") {
          ArchivedToggle(state.archived.size, state.showArchived, onToggleArchived)
        }
        if (state.showArchived) {
          items(state.archived, key = { "a-" + it.id.value.toString() }) { row ->
            CategoryRow(row, onEdit = {}, archived = true, onUnarchive = { onUnarchive(row.id) })
          }
        }
      }
    }
  }

  when (state.sheet) {
    null -> Unit
    else -> CategoryEditorSheet(
      state = state,
      onDismiss = onDismissSheet,
      onNameChange = onNameChange,
      onIconChange = onIconChange,
      onColorChange = onColorChange,
      onArchive = onArchive,
      onSave = onSave,
    )
  }
}

@Composable
private fun Summary(summary: CategorySummaryUi) {
  Text(
    text = stringResource(
      R.string.categories_summary,
      summary.defaultCount,
      summary.customCount,
      summary.transactionCount,
    ),
    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    style = FlowFinTheme.typography.caption,
    color = FlowFinTheme.colors.textSoft,
  )
}

@Composable
private fun SectionLabel(text: String, count: Int) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = text.uppercase(),
      style = FlowFinTheme.typography.label,
      color = FlowFinTheme.colors.textSoft,
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = "· $count",
      style = FlowFinTheme.typography.caption,
      color = FlowFinTheme.colors.textFaint,
    )
  }
}

@Composable
private fun CategoryRow(
  row: CategoryRowUi,
  onEdit: (CategoryId) -> Unit,
  archived: Boolean = false,
  onUnarchive: (() -> Unit)? = null,
) {
  val palette = FlowFinTheme.colors
  // Defaults are immutable, so they get no chevron and no tap target — a row
  // that opened an editor which refuses every edit would be a lie.
  val editable = !row.isDefault && !archived
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .then(if (editable) Modifier.clickable { onEdit(row.id) } else Modifier)
      .padding(vertical = 11.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FlowFinTileIcon(
      icon = categoryIcon(row.iconKey),
      tint = categoryColor(row.colorKey),
      size = 34.dp,
    )
    Spacer(Modifier.width(12.dp))
    Column(Modifier.weight(1f)) {
      Text(
        text = row.name,
        style = FlowFinTheme.typography.bodyLg,
        color = if (archived) palette.textSoft else palette.text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      val meta = if (row.isDefault) stringResource(R.string.categories_locked) else row.meta?.asString()
      if (meta != null) {
        Text(
          text = meta,
          modifier = Modifier.padding(top = 2.dp),
          style = FlowFinTheme.typography.caption,
          color = palette.textFaint,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
    Spacer(Modifier.width(8.dp))
    CountPill(row.count)
    if (onUnarchive != null) {
      Spacer(Modifier.width(8.dp))
      Text(
        text = stringResource(R.string.categories_restore).uppercase(),
        modifier = Modifier
          .clip(RoundedCornerShape(8.dp))
          .clickable(onClick = onUnarchive)
          .padding(horizontal = 10.dp, vertical = 6.dp),
        style = FlowFinTheme.typography.caption,
        color = palette.text,
      )
    } else if (editable) {
      Spacer(Modifier.width(6.dp))
      Icon(
        imageVector = FlowFinIcons.ChevronRight,
        contentDescription = null,
        modifier = Modifier.size(14.dp),
        tint = palette.textFaint,
      )
    }
  }
}

@Composable
private fun CountPill(count: Int) {
  val palette = FlowFinTheme.colors
  Text(
    text = count.toString(),
    modifier = Modifier
      .clip(RoundedCornerShape(7.dp))
      .background(palette.surface3)
      .padding(horizontal = 8.dp, vertical = 3.dp),
    style = FlowFinTheme.typography.caption,
    color = if (count == 0) palette.textFaint else palette.text,
  )
}

@Composable
private fun ArchivedToggle(count: Int, expanded: Boolean, onClick: () -> Unit) {
  val palette = FlowFinTheme.colors
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 18.dp)
      .clickable(onClick = onClick)
      .padding(vertical = 10.dp),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = pluralStringResource(R.plurals.categories_archived_toggle, count, count),
      style = FlowFinTheme.typography.caption,
      color = palette.textSoft,
    )
    Spacer(Modifier.width(6.dp))
    Icon(
      imageVector = FlowFinIcons.ChevronRight,
      contentDescription = null,
      modifier = Modifier.size(14.dp),
      tint = palette.textFaint,
    )
  }
}

@Composable
private fun CategoryEditorSheet(
  state: CategoriesUiState,
  onDismiss: () -> Unit,
  onNameChange: (String) -> Unit,
  onIconChange: (String) -> Unit,
  onColorChange: (String) -> Unit,
  onArchive: (CategoryId) -> Unit,
  onSave: () -> Unit,
) {
  val palette = FlowFinTheme.colors
  val editing = state.sheet as? CategorySheet.Edit
  FlowFinModalBottomSheet(onDismissRequest = onDismiss) {
    FlowFinSheetHeader(
      title = stringResource(if (editing != null) R.string.categories_edit else R.string.categories_add),
      onClose = onDismiss,
    )
    // The icon and colour grids plus the actions are taller than the sheet, and
    // the keyboard takes another chunk — without both of these, Save sits off
    // the bottom with no way to reach it.
    Column(
      modifier = Modifier
        .weight(1f, fill = false)
        .verticalScroll(rememberScrollState())
        .imePadding()
        .padding(horizontal = HORIZONTAL)
        .padding(top = 20.dp, bottom = 12.dp),
    ) {
      FlowFinTextField(
        value = state.form.name,
        onValueChange = onNameChange,
        label = stringResource(R.string.categories_field_name),
        placeholder = stringResource(R.string.categories_name_placeholder),
        keyboardOptions = KeyboardOptions(
          capitalization = KeyboardCapitalization.Words,
          imeAction = ImeAction.Done,
        ),
      )

      PickerLabel(stringResource(R.string.categories_field_icon))
      KeyGrid(
        keys = CATEGORY_ICON_KEYS,
        selected = state.form.iconKey,
        ringShape = RoundedCornerShape(TILE_SIZE_RADIUS + CELL_PADDING),
        ringColor = categoryColor(state.form.colorKey),
        onSelect = onIconChange,
      ) { key, isSelected ->
        FlowFinTileIcon(
          icon = categoryIcon(key),
          tint = if (isSelected) categoryColor(state.form.colorKey) else palette.textSoft,
          size = 40.dp,
        )
      }

      PickerLabel(stringResource(R.string.categories_field_color))
      KeyGrid(
        keys = CATEGORY_COLOR_KEYS,
        selected = state.form.colorKey,
        ringShape = CircleShape,
        ringColor = categoryColor(state.form.colorKey),
        onSelect = onColorChange,
      ) { key, isSelected ->
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(categoryColor(key)),
        )
      }

      FlowFinButton(
        onClick = onSave,
        text = stringResource(R.string.categories_save),
        modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        enabled = state.form.canSave,
      )
      if (editing != null) {
        FlowFinOutlinedButton(
          onClick = { onArchive(editing.id) },
          text = stringResource(R.string.categories_archive),
          modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        )
      }
    }
  }
}

@Composable
private fun PickerLabel(text: String) {
  Text(
    text = text.uppercase(),
    modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
    style = FlowFinTheme.typography.label,
    color = FlowFinTheme.colors.textSoft,
  )
}

/** A wrapping grid of selectable keys — used for both the icon and colour picks. */
/**
 * A wrapping grid of selectable keys — used for both the icon and colour picks.
 *
 * [ringShape] is the caller's job because the selection ring has to stay
 * concentric with what it surrounds: a rounded tile's ring takes the tile's
 * radius plus [CELL_PADDING], a circular swatch's ring is simply a circle.
 */
@Composable
private fun KeyGrid(
  keys: List<String>,
  selected: String,
  ringShape: Shape,
  ringColor: Color,
  onSelect: (String) -> Unit,
  cell: @Composable (key: String, isSelected: Boolean) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    keys.chunked(6).forEach { rowKeys ->
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        rowKeys.forEach { key ->
          val isSelected = key == selected
          // No cell background: the icon draws its own tile and the swatch is
          // its own shape, so a plate behind either just doubles it. Selection
          // is a tinted ring — the icon's own tint is invisible while the
          // colour is still the neutral default.
          Box(
            modifier = Modifier
              .clip(ringShape)
              .then(
                if (isSelected) Modifier.border(1.5.dp, ringColor, ringShape) else Modifier,
              )
              .clickable { onSelect(key) }
              .padding(CELL_PADDING),
            contentAlignment = Alignment.Center,
          ) {
            cell(key, isSelected)
          }
        }
      }
    }
  }
}

@Preview(name = "Categories", widthDp = 390, heightDp = 844)
@Composable
private fun PreviewCategories() = FlowFinTheme {
  CategoriesScreen(
    state = CategoriesUiState(),
    snackbarHostState = androidx.compose.runtime.remember { SnackbarHostState() },
  )
}
