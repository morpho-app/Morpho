import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.kspPlugin)
    alias(libs.plugins.kotlinxAbiPlugin)
    alias(libs.plugins.compose.compiler)

    alias(libs.plugins.androidApplication)
    id("kotlin-parcelize")
    //id("kotlin-kapt")

    //id("com.rickclephas.kmp.nativecoroutines") version "1.0.0-ALPHA-27"
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            freeCompilerArgs.addAll(
                "-P",
                "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.morpho.app.CommonParcelize",
            )
        }
        compilations.all {
            kotlinOptions {
                jvmTarget = "11"

            }
            //move from the deprecated above to this
//            compileJavaTaskProvider.configure {
//                jvm
//            }

        }

    }
    
    jvm("desktop")
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "MorphoApp"
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
            implementation(libs.accompanist.permissions)

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

            implementation(libs.kotlin.jwt)

            implementation("androidx.paging:paging-runtime:3.3.0-alpha02")
            implementation("androidx.paging:paging-compose:3.3.0-alpha02")
        }

        commonMain.dependencies {
            implementation("com.morpho:shared")

            implementation("com.russhwolf:multiplatform-settings:1.2.0")
            implementation("com.russhwolf:multiplatform-settings-serialization:1.2.0")
            implementation("com.russhwolf:multiplatform-settings-coroutines:1.2.0")
            implementation("com.russhwolf:multiplatform-settings-datastore:1.2.0")
            implementation("com.russhwolf:multiplatform-settings-no-arg:1.2.0")
            implementation("androidx.datastore:datastore-preferences-core:1.1.1")
            implementation("androidx.datastore:datastore-core:1.1.1")

            implementation("app.cash.paging:paging-common:3.3.0-alpha02-0.5.1")
            implementation("app.cash.paging:paging-compose-common:3.3.0-alpha02-0.5.1")

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


            // Enables FileKit without Compose dependencies
            implementation(libs.filekit.core)

            // Enables FileKit with Composable utilities
            implementation(libs.filekit.compose)

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
            implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
            implementation("cafe.adriel.voyager:voyager-lifecycle-kmp:1.1.0-beta02")
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

            implementation("com.gu.android:toolargetool:0.3.0")
            api("dev.icerock.moko:parcelize:0.9.0")

        }
        nativeMain.dependencies {
            implementation("app.cash.paging:paging-runtime-uikit:3.3.0-alpha02-0.5.1")
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.appdirs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(kotlin("stdlib"))
            implementation(libs.logback.core)
            implementation(libs.logback.classic)
            implementation(libs.nativeparameterstoreaccess)
            implementation(libs.kotlin.jwt)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
            implementation("app.cash.paging:paging-testing:3.3.0-alpha02-0.5.1")


        }
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(compose.desktop.currentOs)
            }
        }
        getByName("commonMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
            }
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
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")

        }

        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
            applicationIdSuffix = ".debug"
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
        viewBinding = true
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
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
                TargetFormat.Rpm,
                TargetFormat.AppImage,
                TargetFormat.Pkg
            )
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