plugins {
  alias(libs.plugins.flowfin.android.library)
  alias(libs.plugins.flowfin.android.library.compose)
}

android {
  namespace = "com.flowfin.core.designsystem"
}

dependencies {
  api(libs.androidx.compose.foundation)
  api(libs.androidx.compose.foundation.layout)
  api(libs.androidx.compose.material.iconsExtended)
  api(libs.androidx.compose.material3)
  api(libs.androidx.compose.material3.adaptive)
  api(libs.androidx.compose.runtime)
  api(libs.androidx.compose.ui.tooling.preview)
  api(libs.kotlinx.datetime)

  debugApi(libs.androidx.compose.ui.tooling)
}
