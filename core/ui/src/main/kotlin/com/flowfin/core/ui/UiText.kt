package com.flowfin.core.ui

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource

/**
 * User-facing text that context-free code (ViewModels, mappers) can emit by naming a
 * resource, resolved to a String only at display time via [asString]. Keeps copy out
 * of presentation logic without handing it an Android Context.
 *
 * A [Res] arg may itself be a [UiText]: nested values are resolved before formatting,
 * so a template can splice in another resource — e.g. a "Today" / weekday prefix into
 * a date label.
 */
sealed interface UiText {
  data class Res(@param:StringRes val id: Int, val args: List<Any> = emptyList()) : UiText

  data class Plural(@param:PluralsRes val id: Int, val count: Int, val args: List<Any> = emptyList()) : UiText

  data class Raw(val value: String) : UiText
}

@Composable
fun UiText.asString(): String = when (this) {
  is UiText.Raw -> value
  is UiText.Res -> stringResource(id, *resolveArgs(args))
  // The quantity doubles as the sole format arg unless the caller supplies its own.
  is UiText.Plural -> pluralStringResource(id, count, *resolveArgs(args.ifEmpty { listOf(count) }))
}

@Composable
private fun resolveArgs(args: List<Any>): Array<Any> {
  val resolved = arrayOfNulls<Any>(args.size)
  for (i in args.indices) {
    val arg = args[i]
    resolved[i] = if (arg is UiText) arg.asString() else arg
  }
  @Suppress("UNCHECKED_CAST")
  return resolved as Array<Any>
}

/**
 * Resolve outside composition — for one-shot consumers like a snackbar shown from a
 * coroutine, where [asString] (a composition API) can't be called.
 */
fun UiText.resolve(context: Context): String = when (this) {
  is UiText.Raw -> value
  is UiText.Res -> context.getString(id, *resolveArgs(context, args))
  is UiText.Plural ->
    context.resources.getQuantityString(id, count, *resolveArgs(context, args.ifEmpty { listOf(count) }))
}

private fun resolveArgs(context: Context, args: List<Any>): Array<Any> {
  val resolved = arrayOfNulls<Any>(args.size)
  for (i in args.indices) {
    val arg = args[i]
    resolved[i] = if (arg is UiText) arg.resolve(context) else arg
  }
  @Suppress("UNCHECKED_CAST")
  return resolved as Array<Any>
}
