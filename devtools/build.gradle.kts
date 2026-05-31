plugins {
  alias(libs.plugins.flowfin.android.library)
  alias(libs.plugins.flowfin.android.library.compose)
  alias(libs.plugins.flowfin.koin)
}

android {
  namespace = "com.flowfin.devtools"
}

dependencies {
  implementation(projects.core.designsystem)
  implementation(projects.core.domain)
  implementation(projects.core.model)
  implementation(projects.core.database)
  implementation(projects.core.ui)
  implementation(projects.core.resources)

  // The DB wipe runs one SQLDelight transaction (Transacter lives in the runtime).
  implementation(libs.sqldelight.runtime)

  implementation(libs.androidx.activity.compose)

  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.arrow.core)
}
