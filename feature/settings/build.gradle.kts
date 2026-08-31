plugins {
  alias(libs.plugins.flowfin.android.feature)
}

android {
  namespace = "com.flowfin.feature.settings"
}

dependencies {
  implementation(libs.arrow.core)
  implementation(libs.kotlinx.datetime)
}
