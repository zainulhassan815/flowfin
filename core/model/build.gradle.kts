plugins {
  alias(libs.plugins.flowfin.jvm.library)
  alias(libs.plugins.kotlin.serialization)
}

dependencies {
  implementation(projects.core.common)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
}
