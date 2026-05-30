plugins {
  alias(libs.plugins.flowfin.android.library)
}

android {
  namespace = "com.flowfin.core.resources"

  // Shared keys are feature-prefixed (home_*, nav_*, …); the library convention's
  // auto-derived `core_resources_` prefix would fight that, so opt out of it here.
  resourcePrefix = ""
}
