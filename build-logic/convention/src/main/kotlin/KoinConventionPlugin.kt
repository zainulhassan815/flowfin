import com.android.build.gradle.api.AndroidBasePlugin
import com.flowfin.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Wires Koin into a module.
 *
 * - Pure JVM (`org.jetbrains.kotlin.jvm`) modules get only `koin-core`.
 * - Android modules (library or application) additionally get `koin-android`
 *   and the Compose ViewModel bridge.
 */
class KoinConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    pluginManager.withPlugin("org.jetbrains.kotlin.jvm") {
      dependencies {
        "implementation"(libs.findLibrary("koin-core").get())
      }
    }

    pluginManager.withPlugin("com.android.base") {
      dependencies {
        "implementation"(libs.findLibrary("koin-core").get())
        "implementation"(libs.findLibrary("koin-android").get())
        "implementation"(libs.findLibrary("koin-compose").get())
        "implementation"(libs.findLibrary("koin-compose-viewmodel").get())
        "testImplementation"(libs.findLibrary("koin-test").get())
      }
    }
  }
}
