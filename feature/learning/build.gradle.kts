plugins {
    alias(libs.plugins.memrica.android.feature)
}

android {
    namespace = "app.enjoylearning504.feature.learning"
}

dependencies {
    implementation(projects.core.ads)
}
