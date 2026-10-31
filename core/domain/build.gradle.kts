plugins {
    alias(libs.plugins.memrica.jvm.library)
}

dependencies {
    api(projects.core.model)
    api(libs.kotlinx.coroutines.core)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    testImplementation(libs.turbine)
}
