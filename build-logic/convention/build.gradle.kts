import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
  `kotlin-dsl`
  alias(libs.plugins.android.lint)
}

group = "com.flowfin.buildlogic"

java {
  sourceCompatibility = JavaVersion.VERSION_17
  targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
  compilerOptions {
    jvmTarget = JvmTarget.JVM_17
  }
}

dependencies {
  compileOnly(libs.android.gradlePlugin)
  compileOnly(libs.android.tools.common)
  compileOnly(libs.compose.gradlePlugin)
  compileOnly(libs.kotlin.gradlePlugin)
  compileOnly(libs.sqldelight.gradlePlugin)
  compileOnly(libs.spotless.gradlePlugin)
}

tasks {
  validatePlugins {
    enableStricterValidation = true
    failOnWarning = true
  }
}

gradlePlugin {
  plugins {
    register("androidApplication") {
      id = libs.plugins.flowfin.android.application.asProvider().get().pluginId
      implementationClass = "AndroidApplicationConventionPlugin"
    }
    register("androidApplicationCompose") {
      id = libs.plugins.flowfin.android.application.compose.get().pluginId
      implementationClass = "AndroidApplicationComposeConventionPlugin"
    }
    register("androidLibrary") {
      id = libs.plugins.flowfin.android.library.asProvider().get().pluginId
      implementationClass = "AndroidLibraryConventionPlugin"
    }
    register("androidLibraryCompose") {
      id = libs.plugins.flowfin.android.library.compose.get().pluginId
      implementationClass = "AndroidLibraryComposeConventionPlugin"
    }
    register("androidLint") {
      id = libs.plugins.flowfin.android.lint.get().pluginId
      implementationClass = "AndroidLintConventionPlugin"
    }
    register("androidFeature") {
      id = libs.plugins.flowfin.android.feature.get().pluginId
      implementationClass = "AndroidFeatureConventionPlugin"
    }
    register("androidSqldelight") {
      id = libs.plugins.flowfin.android.sqldelight.get().pluginId
      implementationClass = "AndroidSqldelightConventionPlugin"
    }
    register("jvmLibrary") {
      id = libs.plugins.flowfin.jvm.library.get().pluginId
      implementationClass = "JvmLibraryConventionPlugin"
    }
    register("koin") {
      id = libs.plugins.flowfin.koin.get().pluginId
      implementationClass = "KoinConventionPlugin"
    }
    register("root") {
      id = libs.plugins.flowfin.root.get().pluginId
      implementationClass = "RootPlugin"
    }
  }
}
