plugins {
  alias(libs.plugins.flowfin.android.library)
  alias(libs.plugins.flowfin.android.library.compose)
  alias(libs.plugins.flowfin.koin)
}

android {
  namespace = "com.flowfin.core.ui"
}

dependencies {
  api(projects.core.designsystem)
  api(projects.core.model)
  api(projects.core.resources)

  implementation(libs.kotlinx.datetime)

  testImplementation(libs.kotlin.test)
  testImplementation(libs.kotlin.test.junit)
}
