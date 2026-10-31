plugins {
    alias(libs.plugins.memrica.android.feature)
}

android {
    namespace = "app.enjoylearning504.feature.quiz"
}

dependencies {
    implementation(projects.core.ads)
}
