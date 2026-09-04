// Persian AI Voice Assistant - Settings

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

rootProject.name = "PersianVoiceAssistant"

include(":app")

// Domain
include(":domain:model")
include(":domain:repository")
include(":domain:usecase")

// Data
include(":data:model")
include(":data:datasource")
include(":data:repository")

// Core
include(":core:common")
include(":core:database")
include(":core:network")
include(":core:ai")
include(":core:voice")
include(":core:permissions")

// Tools
include(":tools:phone")
include(":tools:contacts")
include(":tools:sms")
include(":tools:calendar")
include(":tools:alarm")
include(":tools:browser")
include(":tools:weather")
include(":tools:calculator")
include(":tools:device")

// Feature
include(":feature:home")
include(":feature:conversation")
include(":feature:settings")
include(":feature:memory")
include(":feature:tools")