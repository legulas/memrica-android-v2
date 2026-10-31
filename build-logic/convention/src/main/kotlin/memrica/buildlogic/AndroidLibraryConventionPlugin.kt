package memrica.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        pluginManager.apply("com.android.library")
        extensions.configure<LibraryExtension> {
            compileSdk {
                version = release(libs.int("compileSdk")) {
                    minorApiLevel = libs.int("compileSdkMinor")
                }
            }
            defaultConfig {
                minSdk = libs.int("minSdk")
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }
            compileOptions {
                sourceCompatibility = JAVA_VERSION
                targetCompatibility = JAVA_VERSION
            }
            testOptions.unitTests.isReturnDefaultValues = true
        }
        dependencies {
            add("implementation", platform(libs.lib("koin-bom")))
            add("implementation", libs.lib("koin-core"))
            add("testImplementation", libs.lib("junit"))
            add("testImplementation", libs.lib("kotlinx-coroutines-test"))
        }
    }
}
