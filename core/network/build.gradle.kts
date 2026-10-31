plugins {
    alias(libs.plugins.memrica.android.library)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.enjoylearning504.core.network"
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "CONFIG_BASE_URL", "\"http://zhi-one-tech.ir/504/\"")
    }
    buildTypes {
        debug {
            // The emulator reaches the host machine's localhost at 10.0.2.2
            buildConfigField("String", "MEMRICA_BASE_URL", "\"http://10.0.2.2:8080/\"")
        }
        release {
            buildConfigField("String", "MEMRICA_BASE_URL", "\"https://api.memrica.app/\"")
        }
    }
}

dependencies {
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    api(libs.ktor.client.auth)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.koin.android)
    testImplementation(libs.ktor.client.mock)
}
