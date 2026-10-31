plugins {
    alias(libs.plugins.memrica.android.application)
    alias(libs.plugins.memrica.android.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "app.enjoylearning504"

    defaultConfig {
        // Same ID as the store app, so v2 installs as an update and keeps users' data
        applicationId = "app.enjoylearning504"
        versionCode = 26
        versionName = "2.0.0"
        manifestPlaceholders["TapsellMediationAppKey"] =
            "jacfnejlndpjomobesoqbrdshsadqgkfqiskisacgrftsrinaqrrtjcrcmergbksfgjlca"
    }

    androidResources {
        generateLocaleConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(projects.core.model)
    implementation(projects.core.domain)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.network)
    implementation(projects.core.audio)
    implementation(projects.core.ads)
    implementation(projects.core.designsystem)
    implementation(projects.core.ui)
    implementation(projects.feature.auth)
    implementation(projects.feature.home)
    implementation(projects.feature.lesson)
    implementation(projects.feature.word)
    implementation(projects.feature.learning)
    implementation(projects.feature.quiz)
    implementation(projects.feature.difficult)
    implementation(projects.feature.info)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.serialization.json)
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    testImplementation(libs.junit)
    testImplementation(libs.koin.test)
    testImplementation(libs.koin.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
