plugins {
  alias(libs.plugins.flowfin.android.feature)
}

android {
  namespace = "com.flowfin.feature.transactions"
}

dependencies {
  implementation(libs.formz)
  implementation(libs.arrow.core)
  implementation(libs.kotlinx.datetime)

  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.turbine)
}
