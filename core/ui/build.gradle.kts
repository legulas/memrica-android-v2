plugins {
    alias(libs.plugins.memrica.android.library)
    alias(libs.plugins.memrica.android.compose)
}

android {
    namespace = "app.enjoylearning504.core.ui"
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.designsystem)
    implementation(libs.androidx.core.ktx)
}
