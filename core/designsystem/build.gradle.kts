plugins {
    alias(libs.plugins.memrica.android.library)
    alias(libs.plugins.memrica.android.compose)
}

android {
    namespace = "app.enjoylearning504.core.designsystem"
}

dependencies {
    api(projects.core.model)
    api(libs.androidx.compose.material.icons.core)
}
