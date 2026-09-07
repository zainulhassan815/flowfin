package com.flowfin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.model.ThemePreference
import com.flowfin.core.model.UserSettings
import com.flowfin.notifications.notificationBackStack
import com.flowfin.ui.DevToolsHost
import com.flowfin.ui.FlowFinApp
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

  // Where a tapped notification wants to land. The activity is singleTop, so a
  // second tap arrives at onNewIntent rather than building a fresh activity.
  private val pendingDeepLink = mutableStateOf<List<NavKey>?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    pendingDeepLink.value = intent.notificationBackStack()
    setContent {
      FlowFinRoot(
        deepLink = pendingDeepLink,
        onDeepLinkHandled = { pendingDeepLink.value = null },
      )
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    pendingDeepLink.value = intent.notificationBackStack()
  }
}

@Composable
private fun FlowFinRoot(deepLink: State<List<NavKey>?>, onDeepLinkHandled: () -> Unit) {
  KoinContext {
    val settings = koinInject<SettingsRepository>()
    val preference by settings.observe().collectAsStateWithLifecycle(UserSettings())
    val dark = when (preference.theme) {
      ThemePreference.LIGHT -> false
      ThemePreference.DARK -> true
      ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }

    // Bars stay transparent and edge-to-edge; only the icon tint follows the
    // palette, so it has to be re-applied whenever the preference changes.
    val activity = LocalActivity.current
    LaunchedEffect(dark, activity) {
      val style = if (dark) {
        SystemBarStyle.dark(Color.TRANSPARENT)
      } else {
        SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
      }
      (activity as? ComponentActivity)?.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }

    FlowFinTheme(darkTheme = dark) {
      Surface(modifier = Modifier.fillMaxSize(), color = FlowFinTheme.colors.bg) {
        // Debug builds overlay a hideable dev-tools launcher; release is a passthrough.
        DevToolsHost {
          FlowFinApp(deepLink = deepLink.value, onDeepLinkHandled = onDeepLinkHandled)
        }
      }
    }
  }
}
