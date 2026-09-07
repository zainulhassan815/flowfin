plugins {
  alias(libs.plugins.flowfin.android.feature)
}

android {
  namespace = "com.flowfin.feature.settings"
}

dependencies {
  implementation(libs.androidx.activity.compose)
  implementation(libs.arrow.core)
  implementation(libs.kotlinx.datetime)
}
