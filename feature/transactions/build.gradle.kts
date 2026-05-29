plugins {
  alias(libs.plugins.flowfin.android.feature)
}

android {
  namespace = "com.flowfin.feature.transactions"
}

dependencies {
  implementation(libs.formz)
  implementation(libs.arrow.core)

  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
}
