plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "global.msnthrp.mokshan.android.core.utils"
    compileSdk = libs.versions.compile.sdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.min.sdk.get().toInt()
    }
    buildFeatures {
        compose = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    debugImplementation(libs.compose.ui.tooling)
}
