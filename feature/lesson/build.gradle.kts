plugins {
    alias(libs.plugins.memrica.android.feature)
}

android {
    namespace = "app.enjoylearning504.feature.lesson"
}

dependencies {
    implementation(projects.core.ads)
}
