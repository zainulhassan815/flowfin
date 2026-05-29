plugins {
  alias(libs.plugins.flowfin.android.feature)
}

android {
  namespace = "com.flowfin.feature.home"
}

dependencies {
  implementation(libs.kotlinx.datetime)
}
