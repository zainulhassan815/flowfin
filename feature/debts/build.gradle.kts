plugins {
  alias(libs.plugins.flowfin.android.feature)
}

android {
  namespace = "com.flowfin.feature.debts"
}

dependencies {
  implementation(libs.kotlinx.datetime)
}
