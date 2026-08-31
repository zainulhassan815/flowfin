plugins {
  alias(libs.plugins.flowfin.android.feature)
}

android {
  namespace = "com.flowfin.feature.reports"
}

dependencies {
  implementation(libs.kotlinx.datetime)
}
