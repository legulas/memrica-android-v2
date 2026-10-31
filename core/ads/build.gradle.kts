plugins {
    alias(libs.plugins.memrica.android.library)
    alias(libs.plugins.memrica.android.compose)
}

android {
    namespace = "app.enjoylearning504.core.ads"
}

dependencies {
    implementation(projects.core.designsystem)
    implementation(libs.tapsell.mediation)
    implementation(libs.tapsell.mediation.legacy)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
}
