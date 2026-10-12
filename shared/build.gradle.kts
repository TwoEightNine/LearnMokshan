import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
}

kotlin {
    android {
        namespace = "global.msnthrp.mokshan"
        compileSdk = libs.versions.compile.sdk.get().toInt()
        minSdk = libs.versions.min.sdk.get().toInt()

        withHostTest {}

        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.negotiation)
            implementation(libs.ktor.client.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.koin.core)
            implementation(libs.androidx.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        commonTest.dependencies {
            implementation(libs.okio)
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
    }
}

room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
}
