import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.room) apply false
    alias(libs.plugins.ksp) apply false
}

android {
    namespace = "global.msnthrp.learnmokshan"
    compileSdk = 34
    defaultConfig {
        applicationId = "global.msnthrp.learnmokshan"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "0.1.3"
        resourceConfigurations.addAll(listOf("en", "ru"))
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        val projectDir = "../../"
        val propFile = file(projectDir + "local.properties")
        val props = Properties()
        props.load(FileInputStream(propFile))
        create("release") {
            storeFile = file(projectDir + props["storeFile"] as String)
            storePassword = props["storePassword"] as? String
            keyAlias = props["keyAlias"] as? String
            keyPassword = props["keyPassword"] as? String
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
            signingConfig = signingConfigs.getByName("release")
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
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

    implementation(libs.androidx.datastore)
    implementation(libs.androidx.datastore.core)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.browser)
    implementation(libs.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.androidx.junit.ktx)
    debugImplementation(libs.compose.ui.tooling)

    testImplementation(libs.kotlin.test)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
}