package com.flowfin

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.core.domain.repository.SettingsRepository
import com.flowfin.core.model.ThemePreference
import com.flowfin.core.model.UserSettings
import com.flowfin.ui.DevToolsHost
import com.flowfin.ui.FlowFinApp
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    super.onCreate(savedInstanceState)
    setContent { FlowFinRoot() }
  }
}

@Composable
private fun FlowFinRoot() {
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
          FlowFinApp()
        }
      }
    }
  }
}
