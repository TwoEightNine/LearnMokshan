enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Learn_Mokshan"
include(":android:app")
include(":android:core:designsystem")
include(":shared")
include(":android:core:utils")
include(":android:features:phrasebook")
include(":android:features:ispeak")
include(":android:features:appinfo")
include(":android:features:articles")
include(":android:core:navigation")
include(":android:features:lessons")
include(":android:core:arch")
