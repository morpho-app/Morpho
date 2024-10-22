

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.jetbrainsCompose).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.kotlinxSerialization).apply(false)
    alias(libs.plugins.kspPlugin).apply(false)
    alias(libs.plugins.kotlinxAbiPlugin).apply(false)

    alias(libs.plugins.compose.compiler).apply(false)
    id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-27" apply false
    alias(libs.plugins.kotlinParcelize).apply(false)
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidLibrary).apply(false)
    id("com.codingfeline.buildkonfig") version "0.15.2" apply false

}

