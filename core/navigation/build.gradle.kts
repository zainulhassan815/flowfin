plugins {
  alias(libs.plugins.flowfin.android.library)
  alias(libs.plugins.flowfin.android.library.compose)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.flowfin.core.navigation"
}

dependencies {
  api(libs.androidx.navigation3.runtime)

  implementation(libs.androidx.compose.runtime)
  implementation(libs.androidx.lifecycle.viewModel.navigation3)
  implementation(libs.kotlinx.serialization.json)
}
