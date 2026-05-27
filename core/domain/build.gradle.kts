plugins {
  alias(libs.plugins.flowfin.jvm.library)
}

dependencies {
  api(projects.core.common)
  api(projects.core.model)

  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.arrow.core)
}
