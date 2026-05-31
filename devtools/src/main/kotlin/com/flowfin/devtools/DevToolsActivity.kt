package com.flowfin.devtools

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.material3.SnackbarHostState
import com.flowfin.core.designsystem.theme.FlowFinTheme
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules

/**
 * Debug-only launcher screen ("FlowFin Dev"), merged into the debug APK via
 * `debugImplementation` only. Runs in the app process, so the global Koin the
 * app's `Application` started is already up — we load [devModule] once on top of
 * it and resolve [DevScenarios] from the real repositories.
 */
class DevToolsActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val koin = GlobalContext.get()
    val scenarios = koin.getOrNull<DevScenarios>() ?: run {
      loadKoinModules(devModule)
      koin.get<DevScenarios>()
    }

    setContent {
      FlowFinTheme {
        val scope = rememberCoroutineScope()
        val snackbar = remember { SnackbarHostState() }
        var busy by remember { mutableStateOf(false) }

        fun run(op: suspend () -> String) {
          if (busy) return
          busy = true
          scope.launch {
            val message = op()
            busy = false
            snackbar.showSnackbar(message)
          }
        }

        DevToolsScreen(
          busy = busy,
          snackbarHostState = snackbar,
          onClose = { finish() },
          onScenario = { scenario -> run { scenarios.run(scenario) } },
          onWipe = { run { scenarios.wipe() } },
          onReseed = { run { scenarios.reseedCategories() } },
        )
      }
    }
  }
}
