// Declares every plugin used across all subprojects so the build script classpath
// is consistent. Subprojects apply these via the `flowfin.*` convention plugins.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.android.lint) apply false
  alias(libs.plugins.android.test) apply false
  alias(libs.plugins.compose) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  alias(libs.plugins.sqldelight) apply false
  alias(libs.plugins.spotless) apply false
  alias(libs.plugins.flowfin.root)
}
