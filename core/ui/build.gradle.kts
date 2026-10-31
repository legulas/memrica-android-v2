plugins {
    alias(libs.plugins.memrica.android.library)
    alias(libs.plugins.memrica.android.compose)
}

android {
    namespace = "app.enjoylearning504.core.ui"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.designsystem)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.androidx.compose)
}
