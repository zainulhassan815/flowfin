package com.flowfin

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/** Wires Compose dependencies and tooling onto an application or library module. */
internal fun Project.configureAndroidCompose(commonExtension: CommonExtension) {
  commonExtension.apply {
    buildFeatures.compose = true

    dependencies {
      val bom = libs.findLibrary("androidx-compose-bom").get()
      "implementation"(platform(bom))
      "androidTestImplementation"(platform(bom))
      "implementation"(libs.findLibrary("androidx-compose-ui-tooling-preview").get())
      "debugImplementation"(libs.findLibrary("androidx-compose-ui-tooling").get())
    }
  }
}
