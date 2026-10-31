plugins {
    alias(libs.plugins.memrica.android.library)
}

android {
    namespace = "app.enjoylearning504.core.data"
}

dependencies {
    api(projects.core.domain)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.core.ktx)
    implementation(libs.koin.android)
    testImplementation(libs.turbine)
    testImplementation(libs.ktor.client.core)
}

dependencies {
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.room.runtime)
}
