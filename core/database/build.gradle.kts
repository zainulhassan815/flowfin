plugins {
  alias(libs.plugins.flowfin.android.library)
  alias(libs.plugins.flowfin.android.sqldelight)
  alias(libs.plugins.flowfin.koin)
}

android {
  namespace = "com.flowfin.core.database"
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
}
