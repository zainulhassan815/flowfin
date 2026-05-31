package com.flowfin.ui

import androidx.compose.runtime.Composable

/**
 * Release variant: a pure passthrough. The dev-tools module isn't on the release
 * classpath and no chip or state exists here — the wrapper compiles away.
 */
@Composable
fun DevToolsHost(content: @Composable () -> Unit) = content()
