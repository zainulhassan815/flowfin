package com.flowfin

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.flowfin.core.designsystem.theme.FlowFinTheme
import com.flowfin.ui.DevToolsHost
import com.flowfin.ui.FlowFinApp
import org.koin.compose.KoinContext

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    installSplashScreen()
    // Draw under the system bars; both stay transparent with light icons since the
    // app is always dark-themed. Screens own their own insets from here on.
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
    )
    super.onCreate(savedInstanceState)
    setContent { FlowFinRoot() }
  }
}

@Composable
private fun FlowFinRoot() {
  KoinContext {
    FlowFinTheme {
      Surface(modifier = Modifier.fillMaxSize(), color = FlowFinTheme.colors.bg) {
        // Debug builds overlay a hideable dev-tools launcher; release is a passthrough.
        DevToolsHost {
          FlowFinApp()
        }
      }
    }
  }
}
