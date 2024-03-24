plugins {
    id("java-library")
    alias(libs.plugins.jetbrainsKotlinJvm)
    id("kotlinx-serialization")
    id("org.jetbrains.kotlin.plugin.serialization")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.ktor.logging)
    implementation(libs.slf4j.api)
    implementation(libs.oshai.kotlin.logging.jvm)
    implementation(libs.kotlin.reflect)
    implementation(libs.okio)
    implementation(libs.ktor.cio)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.immutable)
    implementation(libs.kotlinx.serialization.cbor)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.contentnegotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.websockets)
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.client.auth)

    implementation(libs.kstore)
    implementation(libs.kstore.file)
}