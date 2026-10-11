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
| iOS | Placeholder | Builds a SwiftUI shell around the shared framework but does not expose the Android feature set. |

## Platform support

| Platform | Repository target | Runtime status |
| --- | --- | --- |
| Android | API 26 minimum, API 34 target | Primary supported application. |
| iOS device | `iosArm64` | Placeholder application shell. |
| iOS simulator | `iosSimulatorArm64` | Apple Silicon simulator only; the x64 target is commented out. |

## Technology

- Kotlin 2.0.21 and Kotlin Multiplatform for shared domain, network, and storage
  code.
- Jetpack Compose and Material 3 for Android UI.
- Koin for dependency injection.
- Ktor and Kotlin serialization for content loading.
- Room for the learned-word dictionary and DataStore for lesson progress.
- SwiftUI and CocoaPods for the current iOS shell.
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
iosApp/                SwiftUI application and CocoaPods project
shared/                Kotlin Multiplatform domain, data, use-case, and platform code
gradle/libs.versions.toml
settings.gradle.kts
```

## Prerequisites

For Android development:

- Android Studio with Android SDK 34 and SDK Build Tools 34 installed.
- JDK 17.
- Gradle 8.9 until the repository's wrapper is restored.

Android Gradle Plugin 8.7.x requires Gradle 8.9 and JDK 17; see the
[official compatibility table](https://developer.android.com/build/releases/agp-8-7-0-release-notes#compatibility).

For optional iOS inspection or development, use macOS with Xcode and CocoaPods.
The shared module declares iOS 14.0 as its CocoaPods deployment target, while
the application target currently uses iOS 16.0.

## Configuration

Create a root `local.properties` file. It is ignored by Git and must never be
committed. Use absolute or repository-relative paths as appropriate for your
machine, and keep real signing secrets outside source control.

```properties
sdk.dir=/absolute/path/to/Android/sdk
storeFile=keystore-local.jks
storePassword=<local-secret>
keyAlias=<local-alias>
keyPassword=<local-secret>
```

The current Android build script reads all four signing properties while the
project is being configured, even for a debug build. The keystore only needs to
be valid when a signed release is built, but all keys must currently be present
in `local.properties`. Files named `keystore-*` are ignored by the repository.

Do not copy credentials from another developer, commit a keystore, or put real
values in documentation or issue reports.

## Build and run

### Important: incomplete Gradle wrapper

The repository tracks `gradlew` and `gradlew.bat`, but does not currently track
`gradle/wrapper/gradle-wrapper.jar` or `gradle-wrapper.properties`. Consequently,
`./gradlew` fails before Gradle starts.

Until the wrapper is restored in a separate build-system change, either:

1. configure Android Studio to use a local Gradle 8.9 installation; or
2. run the tasks below with an installed Gradle 8.9 executable by replacing
   `./gradlew` with `gradle`.

Once the wrapper is complete, build and install the debug application with:

```shell
./gradlew :android:app:assembleDebug
./gradlew :android:app:installDebug
```

The debug application ID is `global.msnthrp.learnmokshan.debug`. Debug builds
load the sandbox lesson and article indexes; release builds load production
indexes.

A release build additionally requires a valid keystore and matching signing
credentials:

```shell
./gradlew :android:app:assembleRelease
```

### iOS shell

The iOS target is useful for shared-framework integration work, but it is not a
complete Learn Mokshan client. After a working Gradle installation or wrapper is
available:

```shell
cd iosApp
pod install
open iosApp.xcworkspace
```

The Kotlin CocoaPods integration builds the `shared` framework. Do not treat a
successful launch of the placeholder SwiftUI view as feature parity with
Android.

## Tests and checks

With a working Gradle 8.9 installation or restored wrapper, the main checks are:

```shell
./gradlew :shared:allTests
./gradlew :android:app:lintDebug
./gradlew :android:app:assembleDebug
```

The shared tests validate lesson content, including translation-hint coverage.
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
