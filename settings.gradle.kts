@file:Suppress("ktlint")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

rootProject.name = "Playdita"

include(
    ":retrograde-util",
    ":retrograde-app-shared",
    ":lemuroid-touchinput",
    ":lemuroid-metadata-libretro-db",
    ":lemuroid-app-ext-free",
    ":app"
)

if (file("lemuroid-app").exists() && !file("app/build.gradle.kts").exists()) {
    project(":app").projectDir = file("lemuroid-app")
}

