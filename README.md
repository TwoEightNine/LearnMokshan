# Learn Mokshan

Learn Mokshan is a mobile learning application for the Mokshan language. It
combines guided vocabulary lessons, grammar and cultural articles, and a
personal dictionary that grows as lessons are completed.

The repository also stores the English and Russian learning content consumed by
the application. The Android application is the usable product today; the iOS
target is an early placeholder rather than a feature-complete app.

## Features

- Guided lessons with word-bank and typed-answer exercises.
- English and Russian interface and learning content.
- Grammar hints and word translations during lessons.
- Topic and lesson progress saved locally on the device.
- A personal Mokshan dictionary populated from completed lessons.
- Grammar, pronunciation, and cultural articles.
- Cached remote content with a stale-cache fallback when a refresh fails.

### Current feature status

| Area | Status | Notes |
| --- | --- | --- |
| Android lessons | Active | Main learning flow, progress, hints, and review exercises. |
| Android dictionary | Active | Shows words learned by completing lessons. |
| Android articles | Active | Renders JSON and Prelinar article formats. |
| Android app information | Active | Version, legal links, and developer contact. |
| Phrasebook | Dormant | Code and content exist, but the main navigation entry is disabled. |
| “I Speak” | Dormant | A UI module exists but is not connected to application navigation. |
| iOS | Placeholder | Standalone SwiftUI shell with no shared-framework or product-feature integration. |

## Platform support

| Platform | Repository target | Runtime status |
| --- | --- | --- |
| Android | API 26 minimum, API 36 target | Primary supported application. |
| iOS | Standalone Xcode project | Buildable placeholder; not a Gradle/KMP target. |

## Technology

- Kotlin 2.4.21 and an Android-only Kotlin Multiplatform library for shared
  domain, network, and storage code.
- Jetpack Compose and Material 3 for Android UI.
- Koin for dependency injection.
- Ktor and Kotlin serialization for content loading.
- Room 3 for the learned-word dictionary and DataStore for lesson progress.
- SwiftUI for the standalone iOS placeholder.
- Gradle version catalogs for dependency and plugin versions.

See [DOCS.md](DOCS.md) for the architecture, data formats, coding conventions,
and detailed maintenance guidance.

## Repository layout

```text
android/
  app/                 Android application, navigation, and platform bindings
  core/                Shared Android architecture, design system, navigation, utilities
  features/            Feature UI modules
content/               English/Russian lessons, articles, phrasebook, and legal text
iosApp/                Standalone SwiftUI placeholder
shared/                Android-only KMP domain, data, use-case, and platform code
gradle/libs.versions.toml
settings.gradle.kts
```

## Prerequisites

For Android development:

- Android Studio with Android SDK 36 and SDK Build Tools 36 installed.
- JDK 17.
- The checked-in Gradle 9.6 wrapper.

Android Gradle Plugin 9.4.x requires Gradle 9.6 and JDK 17; see the
[official compatibility table](https://developer.android.com/build/releases/agp-9-4-0-release-notes#compatibility).

For optional iOS placeholder inspection, use macOS with Xcode. CocoaPods and a
Kotlin framework are not required. The application target uses iOS 16.0.

## Configuration

Create a root `local.properties` file. It is ignored by Git and must never be
committed. Use absolute or repository-relative paths as appropriate for your
machine, and keep real signing secrets outside source control.

```properties
sdk.dir=/absolute/path/to/Android/sdk
```

Only `sdk.dir` belongs in `local.properties`. Release signing is optional and is
configured through all four `mokshan.release.*` properties in the user's Gradle
properties or the equivalent `MOKSHAN_RELEASE_*` environment variables:

```properties
mokshan.release.storeFile=/absolute/path/to/keystore.jks
mokshan.release.storePassword=<local-secret>
mokshan.release.keyAlias=<local-alias>
mokshan.release.keyPassword=<local-secret>
```

When none are set, release builds are unsigned. A partially configured release
fails with a clear error, while debug builds never require signing values.

Do not copy credentials from another developer, commit a keystore, or put real
values in documentation or issue reports.

## Build and run

Build and install the debug application with:

```shell
./gradlew :android:app:assembleDebug
./gradlew :android:app:installDebug
```

The debug application ID is `global.msnthrp.learnmokshan.debug`. Debug builds
load the sandbox lesson and article indexes; release builds load production
indexes.

A release build is unsigned unless complete signing properties are supplied:

```shell
./gradlew :android:app:assembleRelease
```

### iOS shell

The iOS project is a standalone placeholder and is not a complete Learn Mokshan
client. It can be opened directly without Gradle or CocoaPods:

```shell
open iosApp/iosApp.xcodeproj
```

Do not treat a successful launch of the placeholder SwiftUI view as feature
parity with Android.

## Tests and checks

The main checks are:

```shell
./gradlew :shared:allTests
./gradlew testDebugUnitTest
./gradlew :android:app:lintDebug
./gradlew :android:app:assembleDebug
./gradlew :android:app:assembleRelease
```

The shared tests cover the business logic (lesson engine, repositories, network
client, parsers, storage) and validate the `content/` files with the production
parsers, including translation-hint coverage. `testDebugUnitTest` runs the JVM
unit tests of the Android modules (view models, server configuration, locale
mapping). Tests named `*Bug*`/`*Issue*` deliberately pin behaviour that is
currently wrong; see [DOCS.md](DOCS.md).
`ServerConfigTests` is an Android instrumentation test and requires a connected
device or emulator running the non-debug application configuration it is meant
to verify.

## Content and legal material

Application content is versioned in [`content/`](content/). The Android client
loads production content from the repository's `master` branch through
`raw.githubusercontent.com`; debug builds substitute sandbox indexes where
available. Content changes therefore affect released clients without an app
binary update and must be reviewed carefully.

Privacy policy and terms of service source files are in
[`content/legal/`](content/legal/) in English and Russian.

## Project links

- Source repository: [TwoEightNine/LearnMokshan](https://github.com/TwoEightNine/LearnMokshan)
- Technical documentation: [DOCS.md](DOCS.md)
- Instructions for coding agents: [AGENTS.md](AGENTS.md)

No standalone license file is currently included in the repository. Do not
assume reuse rights beyond those explicitly granted by the copyright holder.
