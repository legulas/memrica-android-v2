plugins {
    alias(libs.plugins.memrica.android.library)
}

android {
    namespace = "app.enjoylearning504.core.datastore"
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(libs.koin.android)
}
