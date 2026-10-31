plugins {
    `kotlin-dsl`
}

group = "memrica.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "memrica.android.application"
            implementationClass = "memrica.buildlogic.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "memrica.android.library"
            implementationClass = "memrica.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "memrica.android.compose"
            implementationClass = "memrica.buildlogic.AndroidComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "memrica.android.feature"
            implementationClass = "memrica.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("jvmLibrary") {
            id = "memrica.jvm.library"
            implementationClass = "memrica.buildlogic.JvmLibraryConventionPlugin"
        }
    }
}
