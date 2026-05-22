plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "chat.cabal.network"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
    compileSdkMinor = 0
    buildToolsVersion = "37.0.0"
    ndkVersion = "28.2.13676358"
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_25)
    }
}

dependencies {
    implementation(project(":cable-protocol"))
    implementation(libs.ktor.network)
    implementation(libs.ktor.utils)
    testImplementation(libs.junit)
}
