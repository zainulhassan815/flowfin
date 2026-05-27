import app.cash.sqldelight.gradle.SqlDelightExtension
import com.flowfin.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

/**
 * Wires SQLDelight onto a module. Configures the `FlowFinDatabase` schema
 * in package `com.flowfin.core.database` and pulls in the runtime, the
 * Android driver, and coroutine extensions.
 */
class AndroidSqldelightConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) = with(target) {
    apply(plugin = "app.cash.sqldelight")

    val sqldelightVersion = libs.findVersion("sqldelight").get().requiredVersion

    extensions.configure<SqlDelightExtension> {
      databases.create("FlowFinDatabase") {
        packageName.set("com.flowfin.core.database")
        dialect("app.cash.sqldelight:sqlite-3-38-dialect:$sqldelightVersion")
        verifyMigrations.set(true)
        deriveSchemaFromMigrations.set(false)
      }
    }

    dependencies {
      "implementation"(libs.findLibrary("sqldelight-runtime").get())
      "implementation"(libs.findLibrary("sqldelight-coroutines").get())
      "implementation"(libs.findLibrary("sqldelight-primitive-adapters").get())
      "implementation"(libs.findLibrary("sqldelight-driver-android").get())
    }
  }
}
