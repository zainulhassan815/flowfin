package com.flowfin

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/** Configures shared Android + Kotlin options for application and library modules. */
internal fun Project.configureKotlinAndroid(
  commonExtension: CommonExtension,
) {
  commonExtension.apply {
    compileSdk = 36
    defaultConfig.minSdk = 26

    compileOptions.apply {
      // Java 11 APIs via desugaring on lower min SDKs.
      sourceCompatibility = JavaVersion.VERSION_11
      targetCompatibility = JavaVersion.VERSION_11
      isCoreLibraryDesugaringEnabled = true
    }
  }

  configureKotlin<KotlinAndroidProjectExtension>()

  dependencies {
    "coreLibraryDesugaring"(libs.findLibrary("android-desugarJdkLibs").get())
  }
}

/** Configures Kotlin options for a JVM-only library (no Android). */
internal fun Project.configureKotlinJvm() {
  extensions.configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  configureKotlin<KotlinJvmProjectExtension>()
}

private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
  val warningsAsErrors = providers.gradleProperty("warningsAsErrors")
    .map(String::toBoolean)
    .orElse(false)

  when (this) {
    is KotlinAndroidProjectExtension -> compilerOptions
    is KotlinJvmProjectExtension -> compilerOptions
    else -> error("Unsupported Kotlin project extension: ${T::class}")
  }.apply {
    jvmTarget = JvmTarget.JVM_11
    allWarningsAsErrors = warningsAsErrors
    freeCompilerArgs.add("-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi")
  }
}
