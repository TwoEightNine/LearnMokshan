
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.room)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinCocoapods)
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    cocoapods {
        version = "1.0.0"
        summary = "App for learning the Mokshan language"
        homepage = "https://github.com/TwoEightNine/LearnMokshan"
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "shared"
        }
    }
    
    listOf(
//        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    sourceSets.all {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
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
        }
        commonTest.dependencies {
            implementation(libs.okio)
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.okio)
        }

        androidMain.dependencies {
            implementation(libs.sqlite.bundled)
            implementation(libs.ktor.client.okhttp)
        }

        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
    }
}

android {
    namespace = "global.msnthrp.mokshan"
    compileSdk = 34
    defaultConfig {
        minSdk = 26
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    room {
        schemaDirectory("$projectDir/schemas")
    }
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
//    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
}

tasks.register("assembleDebugXCFramework") {
    dependsOn(
        "linkDebugFrameworkIosArm64",
//        "linkDebugFrameworkIosX64",
        "linkDebugFrameworkIosSimulatorArm64"
    )

    doLast {
        val outputDir = buildDir.resolve("XCFrameworks/debug")
        outputDir.deleteRecursively()
        outputDir.mkdirs()

        val frameworks = listOf(
            "iosArm64" to "debugFramework",
//            "iosX64" to "debugFramework",
            "iosSimulatorArm64" to "debugFramework"
        )

        val frameworkArgs = frameworks.flatMap { (platform, config) ->
            listOf(
                "-framework", buildDir.resolve("bin/$platform/$config/shared.framework").absolutePath
            )
        }

        exec {
            commandLine(
                "xcodebuild",
                "-create-xcframework",
                *frameworkArgs.toTypedArray(),
                "-output", outputDir.resolve("shared.xcframework").absolutePath
            )
        }

        println("XCFramework assembled at: ${outputDir.resolve("shared.xcframework")}")
    }
}


