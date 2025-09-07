// ---- Repositories for plugins (Kotlin/Spring) ----
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
    }
}

// ---- Repositories for all project dependencies ----
dependencyResolutionManagement {
    // אם היה מוגדר FAIL_ON_PROJECT_REPOS – מחליפים לזה
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
}

rootProject.name = "AutofferServer"

include(":AutofferServer", ":gateway", ":AutofferModelsRequests")
