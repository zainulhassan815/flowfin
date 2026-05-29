plugins {
  alias(libs.plugins.flowfin.android.library)
  alias(libs.plugins.flowfin.koin)
}

android {
  namespace = "com.flowfin.core.data"
}

dependencies {
  api(projects.core.domain)
  api(projects.core.model)

  implementation(projects.core.common)
  implementation(projects.core.database)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.sqldelight.coroutines)
  implementation(libs.arrow.core)

  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
  testImplementation(libs.sqldelight.driver.jvm)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.turbine)
}
