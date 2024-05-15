pluginManagement {
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
    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
        repositories {
            google()
            mavenCentral()
        }
    }
    buildscript {
        val kotlin_version = "1.6.10"
        repositories {
            // Make sure that you have the following two repositories
            google()  // Google's Maven repository
            mavenCentral()  // Maven Central repository
        }
    }

    // enableFeaturePreview("VERSION_CATALOGS")

    rootProject.name = "catch-up"
    include(":app")
}