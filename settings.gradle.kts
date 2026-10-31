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
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "memrica-android-v2"

include(":app")
include(":core:model")
include(":core:domain")
include(":core:data")
include(":core:database")
include(":core:datastore")
include(":core:network")
include(":core:audio")
include(":core:ads")
include(":core:designsystem")
include(":core:ui")
include(":feature:auth")
include(":feature:home")
include(":feature:lesson")
include(":feature:word")
include(":feature:learning")
include(":feature:quiz")
include(":feature:difficult")
include(":feature:info")
