plugins {
    alias(libs.plugins.memrica.android.library)
}

android {
    namespace = "app.enjoylearning504.core.audio"
}

dependencies {
    implementation(projects.core.domain)
    implementation(libs.media3.exoplayer)
    implementation(libs.koin.android)
}
