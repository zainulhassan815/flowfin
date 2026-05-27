plugins {
  alias(libs.plugins.flowfin.jvm.library)
}

dependencies {
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.datetime)
  implementation(libs.arrow.core)
}
