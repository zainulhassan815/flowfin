pluginManagement {
  includeBuild("build-logic")
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    google {
      content {
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google.*")
        includeGroupByRegex("androidx.*")
      }
    }
    mavenCentral()
  }
}

rootProject.name = "flowfin"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":app")

include(":core:common")
include(":core:database")
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:model")
include(":core:resources")
include(":core:ui")
include(":core:navigation")

include(":feature:home")
include(":feature:accounts")
include(":feature:transactions")
include(":feature:recurring")
include(":feature:debts")
include(":feature:reports")
include(":feature:settings")

check(JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_17)) {
  """
  FlowFin requires JDK 17+ but is currently using JDK ${JavaVersion.current()}.
  Java Home: [${System.getProperty("java.home")}]
  https://developer.android.com/build/jdks#jdk-config-in-studio
  """.trimIndent()
}
