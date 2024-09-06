rootProject.name = "MorphoMultiplatform"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://jitpack.io")

        maven("https://plugins.gradle.org/m2/")
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        maven("https://jitpack.io")

        mavenCentral()
        google()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://plugins.gradle.org/m2/")
    }
}

includeBuild("Morpho")
includeBuild("Butterfly")