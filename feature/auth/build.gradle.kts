plugins {
    alias(libs.plugins.memrica.android.feature)
}

android {
    namespace = "app.enjoylearning504.feature.auth"
}

dependencies {
    // SMS Retriever: reads the OTP SMS without the SMS permission
    implementation(libs.play.services.auth.api.phone)
}
