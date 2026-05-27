import com.android.build.api.dsl.LibraryExtension
import com.flowfin.configureKotlinAndroid
import com.flowfin.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    apply(plugin = "com.android.library")
    apply(plugin = "flowfin.android.lint")

    extensions.configure<LibraryExtension> {
      configureKotlinAndroid(this)
      defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
      testOptions.targetSdk = 36
      testOptions.animationsDisabled = true
      lint.targetSdk = 36

      // Module-name-derived resource prefix, e.g. `:core:database` -> `core_database_`.
      resourcePrefix = path.split("""\W""".toRegex())
        .drop(1)
        .distinct()
        .joinToString(separator = "_")
        .lowercase() + "_"
    }

    dependencies {
      "implementation"(libs.findLibrary("androidx-tracing-ktx").get())
      "testImplementation"(libs.findLibrary("kotlin-test").get())
      "testImplementation"(libs.findLibrary("junit").get())
    }
  }
}
