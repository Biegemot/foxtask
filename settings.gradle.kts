pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    
    plugins {
        id("com.android.application") version "8.11.1"
        id("org.jetbrains.kotlin.android") version "2.1.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.1.21"
        id("com.google.devtools.ksp") version "2.1.21-2.0.2"
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "FoxTask"
include(":app")
