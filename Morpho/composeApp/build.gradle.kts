import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kspPlugin)
    alias(libs.plugins.kotlinxAbiPlugin)
    //id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-27"
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"

            }

        }

    }
    
    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {

        all {
            //languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            //languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
        }

        val desktopMain by getting
        
        androidMain.dependencies {
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.window)
            implementation(libs.androidx.material3.window.sizeclass)


            // Koin dependency injection
            implementation(libs.koin.android)
            // Java Compatibility
            implementation(libs.koin.android.compat)
            // Jetpack WorkManager
            implementation(libs.koin.androidx.workmanager)
            // Navigation Graph
            implementation(libs.koin.androidx.navigation)
            implementation(libs.koin.androidx.compose)

            // Image loading
            implementation(libs.ktor.client.android)
        }

        commonMain.dependencies {
            implementation("com.morpho:shared")


            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(compose.materialIconsExtended)
            implementation(compose.runtimeSaveable)
            implementation(compose.animation)
            implementation(compose.animationGraphics)

            implementation(libs.constraintlayout.compose.multiplatform)

            // Image loading
            implementation(libs.coil.compose)
            implementation(libs.coil)
            implementation(libs.coil.network.ktor)

            // Multiplatform Palette
            implementation(libs.kmpalette.core)
            implementation(libs.kmpalette.extensions.base64)
            implementation(libs.kmpalette.extensions.bytearray)
            implementation(libs.kmpalette.extensions.libres)
            implementation(libs.kmpalette.extensions.network)
            implementation(libs.kmpalette.extensions.file)

            // Kotlin libraries
            implementation(kotlin("stdlib"))

            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.immutable)
            implementation(libs.kotlinx.serialization.cbor)
            implementation(libs.kotlinx.serialization.json)
            implementation(kotlin("reflect"))

            api(libs.logging)

            // KStore
            implementation(libs.kstore)
            implementation(libs.kstore.file)

            // Koin dependency injection
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.core.coroutines)
            implementation(libs.koin.annotations)
            implementation(libs.koin.compose)

            //api(libs.kmm.viewmodel.core)

            // Ktor networking
            implementation(libs.okio)
            implementation(libs.ktor.cio)
            implementation(libs.ktor.contentnegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.websockets)
            implementation(libs.ktor.client.resources)
            implementation(libs.ktor.client.auth)

            // Voyager
            // Navigator
            implementation(libs.voyager.navigator)
            // Screen Model
            implementation(libs.voyager.screenmodel)
            // BottomSheetNavigator
            implementation(libs.voyager.bottom.sheet.navigator)
            // TabNavigator
            implementation(libs.voyager.tab.navigator)
            // Transitions
            implementation(libs.voyager.transitions)
            // Koin integration
            implementation(libs.voyager.koin)

            // Logging
            implementation(libs.ktor.logging)
            implementation(libs.slf4j.api)
            //implementation(libs.slf4j.simple)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.appdirs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.logback.core)
            implementation(libs.logback.classic)
            implementation(libs.nativeparameterstoreaccess)
        }
    }
}

android {
    namespace = "com.morpho.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.morpho.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    dependencies {
        debugImplementation(libs.compose.ui.tooling)
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
    }


    task("testClasses")
}


compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.morpho.app"
            packageVersion = "1.0.0"
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler) // Run KSP on [commonMain] code
    add("kspAndroid", libs.koin.ksp.compiler)
    //add("kspIosX64", libs.koin.ksp.compiler)
    //add("kspIosArm64", libs.koin.ksp.compiler)
    //add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
}