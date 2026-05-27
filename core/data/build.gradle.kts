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
  implementation(libs.kotlinx.uuid.core)
  implementation(libs.sqldelight.coroutines)
}
