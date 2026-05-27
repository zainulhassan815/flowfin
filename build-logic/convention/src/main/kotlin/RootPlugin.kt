import com.diffplug.gradle.spotless.SpotlessExtension
import com.flowfin.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

/**
 * Applied to the root project. Today this wires Spotless across every
 * subproject so formatting stays consistent without per-module boilerplate.
 */
class RootPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    apply(plugin = "com.diffplug.spotless")

    extensions.configure<SpotlessExtension> {
      val ktlintVersion = libs.findVersion("ktlint").get().requiredVersion
      kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt", "**/generated/**/*.kt")
        ktlint(ktlintVersion).editorConfigOverride(
          mapOf(
            "indent_size" to 2,
            "ktlint_standard_no-wildcard-imports" to "disabled",
          ),
        )
      }
      kotlinGradle {
        target("**/*.gradle.kts")
        targetExclude("**/build/**/*.kts")
        ktlint(ktlintVersion).editorConfigOverride(mapOf("indent_size" to 2))
      }
    }

    subprojects {
      pluginManager.withPlugin("com.android.base") {
        // No-op for now. Reserved for cross-cutting Android subproject config.
      }
    }
  }
}
