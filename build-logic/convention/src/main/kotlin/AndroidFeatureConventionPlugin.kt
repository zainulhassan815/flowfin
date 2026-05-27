import com.android.build.api.dsl.LibraryExtension
import com.flowfin.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.project

/**
 * Convention for a `:feature:*` module — Android library + Compose + Koin,
 * with the shared design-system and domain dependencies wired automatically.
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    apply(plugin = "flowfin.android.library")
    apply(plugin = "flowfin.android.library.compose")
    apply(plugin = "flowfin.koin")

    extensions.configure<LibraryExtension> {
      testOptions.animationsDisabled = true
    }

    dependencies {
      "implementation"(project(":core:designsystem"))
      "implementation"(project(":core:domain"))

      "implementation"(libs.findLibrary("androidx-lifecycle-runtimeCompose").get())
      "implementation"(libs.findLibrary("androidx-lifecycle-viewModelCompose").get())
      "implementation"(libs.findLibrary("androidx-navigation3-runtime").get())
    }
  }
}
