pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "volapex"

include(
    ":shared:domain",
    ":shared:protocol",
    ":desktop:receiver",
    ":desktop:vjoy",
    ":desktop:app",
    ":android:engine",
    ":android:app",
)
