plugins {
  alias(libs.plugins.flowfin.android.application)
  alias(libs.plugins.flowfin.android.application.compose)
  alias(libs.plugins.flowfin.koin)
  alias(libs.plugins.kotlin.serialization)
}

android {
  namespace = "com.flowfin"

  defaultConfig {
    applicationId = "com.flowfin"
    versionCode = 1
    versionName = "0.1.0"
  }

  buildTypes {
    debug {
      applicationIdSuffix = ".debug"
    }
    release {
      isMinifyEnabled = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro",
      )
      // TODO: replace once a release signing config is set up.
      signingConfig = signingConfigs.named("debug").get()
    }
  }

  // Settings shows the running build's version; BuildConfig is where it comes from.
  buildFeatures {
    buildConfig = true
  }

  packaging {
    resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
  }
}

dependencies {
  implementation(projects.core.common)
  implementation(projects.core.designsystem)
  implementation(projects.core.domain)
  implementation(projects.core.data)
  implementation(projects.core.database)
  implementation(projects.core.model)
  implementation(projects.core.resources)
  implementation(projects.core.ui)
  implementation(projects.core.navigation)

  implementation(projects.feature.home)
  implementation(projects.feature.accounts)
  implementation(projects.feature.transactions)
  implementation(projects.feature.recurring)
  implementation(projects.feature.debts)
  implementation(projects.feature.reports)
  implementation(projects.feature.settings)

  // Debug-only developer tools (DB seeding / reset). Absent from release builds.
  debugImplementation(projects.devtools)

  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.core.splashscreen)
  implementation(libs.androidx.lifecycle.runtimeCompose)
  implementation(libs.androidx.lifecycle.viewModel.navigation3)
  implementation(libs.androidx.navigation3.ui)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material3.adaptive)
  implementation(libs.androidx.compose.material3.adaptive.layout)
  implementation(libs.androidx.compose.material3.adaptive.navigation)
  implementation(libs.androidx.compose.material3.adaptive.navigation3)

  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.datetime)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.arrow.core)
}
