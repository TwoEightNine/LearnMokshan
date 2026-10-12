plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.compose.compiler)
}

val releaseSigning = mapOf(
    "storeFile" to providers.gradleProperty("mokshan.release.storeFile")
        .orElse(providers.environmentVariable("MOKSHAN_RELEASE_STORE_FILE")).orNull,
    "storePassword" to providers.gradleProperty("mokshan.release.storePassword")
        .orElse(providers.environmentVariable("MOKSHAN_RELEASE_STORE_PASSWORD")).orNull,
    "keyAlias" to providers.gradleProperty("mokshan.release.keyAlias")
        .orElse(providers.environmentVariable("MOKSHAN_RELEASE_KEY_ALIAS")).orNull,
    "keyPassword" to providers.gradleProperty("mokshan.release.keyPassword")
        .orElse(providers.environmentVariable("MOKSHAN_RELEASE_KEY_PASSWORD")).orNull,
)
val hasCompleteReleaseSigning = releaseSigning.values.all { !it.isNullOrBlank() }
val hasPartialReleaseSigning = releaseSigning.values.any { !it.isNullOrBlank() } &&
    !hasCompleteReleaseSigning

android {
    namespace = "global.msnthrp.learnmokshan"
    compileSdk = libs.versions.compile.sdk.get().toInt()
    defaultConfig {
        applicationId = "global.msnthrp.learnmokshan"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.target.sdk.get().toInt()
        versionCode = 4
        versionName = "0.1.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    androidResources {
        localeFilters += listOf("en", "ru")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    if (hasCompleteReleaseSigning) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(requireNotNull(releaseSigning["storeFile"]))
                storePassword = releaseSigning["storePassword"]
                keyAlias = releaseSigning["keyAlias"]
                keyPassword = releaseSigning["keyPassword"]
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasCompleteReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.shared)
    implementation(projects.android.core.designsystem)
    implementation(projects.android.core.utils)
    implementation(projects.android.core.navigation)
    implementation(projects.android.features.phrasebook)
    implementation(projects.android.features.ispeak)
    implementation(projects.android.features.appinfo)
    implementation(projects.android.features.articles)
    implementation(projects.android.features.lessons)
    implementation(projects.android.features.dictionary)

    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.okio)
    debugImplementation(libs.compose.ui.tooling)

    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.runtime)
    androidTestImplementation(libs.sqlite.bundled)
}

tasks.configureEach {
    if (hasPartialReleaseSigning && name.contains("Release", ignoreCase = true)) {
        doFirst {
            error(
                "Release signing is only partially configured. Set all four " +
                    "mokshan.release.* Gradle properties or MOKSHAN_RELEASE_* environment variables."
            )
        }
    }
}
