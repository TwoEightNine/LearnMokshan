# Learn Mokshan Technical Documentation

This document is the technical source of truth for the current repository. It
describes behavior that exists in source, including incomplete and dormant
areas. When implementation and documentation disagree, verify the code and
update this document in the same change that resolves the mismatch.

## 1. Product and platform status

Learn Mokshan is currently an Android-first language-learning application. Its
active Android navigation exposes lessons, a learned-word dictionary, articles,
and app information. The Phrasebook module is compiled but its navigation item
is commented out. The “I Speak” module is also compiled but has no route. The
iOS application contains only a SwiftUI placeholder and does not initialize or
present the shared product flows.

The UI and content support English (`en`) and Russian (`ru`). On Android,
`DeviceLocaleProviderImpl` maps a Russian device locale to Russian and every
other locale to English.

## 2. Repository architecture

### Gradle modules

All modules are declared in `settings.gradle.kts`.

| Module | Responsibility |
| --- | --- |
| `:shared` | Android-only KMP domain models, use cases, repositories, Ktor data sources, DataStore abstractions, Room database, and Android platform implementations. |
| `:android:app` | Android entry point, top-level navigation, Koin startup, platform bindings, build variants, signing, and app resources. |
| `:android:core:arch` | Generic `BaseViewModel<State>` backed by `StateFlow`. |
| `:android:core:designsystem` | Theme, icons, and reusable Compose components. |
| `:android:core:navigation` | `Router`, `ScreenFactory`, and the Compose navigation host. |
| `:android:core:utils` | Android/Compose utility extensions and build-config abstraction. |
| `:android:features:lessons` | Topic list and interactive lesson UI. |
| `:android:features:dictionary` | Learned-word dictionary UI. |
| `:android:features:articles` | Article list and JART/Prelinar renderer UI. |
| `:android:features:appinfo` | Version, legal, and contact UI. |
| `:android:features:phrasebook` | Dormant phrasebook UI. |
| `:android:features:ispeak` | Dormant “I Speak” UI. |

Outside Gradle, `iosApp/` contains the SwiftUI/Xcode shell and `content/`
contains data served directly to clients.

### Dependency direction

The intended dependency flow is:

```text
Android feature UI
        |
        v
shared use cases / repositories
        |
        v
shared network and storage data sources
        |
        v
platform implementations (Android today)
```

Android features may depend on Android core modules and `:shared`. Shared
`commonMain` must not import Android APIs. Platform facilities are introduced
through common interfaces or `expect` declarations and implemented under
`androidMain`.

### Source sets

- `shared/src/commonMain`: cross-platform models and behavior.
- `shared/src/androidMain`: Android database construction, network cache, and
  date formatting.
- `shared/src/commonTest`: portable content validation.
- `shared/src/androidHostTest`: JVM helpers for the Android KMP host tests.

The shared module exposes only an Android target. The standalone SwiftUI
placeholder does not depend on it.

## 3. Application runtime

### Android startup and dependency injection

`LeMokApplication` starts Koin and loads:

1. shared repository, use-case, network, and storage modules;
2. Android platform modules for Room and network caching;
3. feature ViewModel modules; and
4. Android bindings for locale, files, time, build metadata, and server URLs.

Debug builds bind `SandboxServerConfig(ProdServerConfig())`. Release builds bind
`ProdServerConfig` directly. The service graph is assembled at application
startup; new dependencies should be constructor-injected and registered in the
module that owns them.

### Navigation and screens

`MainActivity` applies `LeMokTheme` and creates a `LeMokNavHost`. Navigation is
represented by a `Router` route string and a `ScreenFactory` composable factory.
The top-level graph contains main, app-info, article, and lesson destinations.

`MainScreen` switches between three active bottom destinations without creating
nested navigation routes:

- lessons (`TopicsListScreenFactory`);
- dictionary (`DictionaryScreenFactory`); and
- articles (`ArticlesListFactory`).

Feature navigation callbacks are passed into factories. Preserve this pattern
instead of reading a global `NavController` from feature UI.

### UI state

Most Android features extend `BaseViewModel<State>`, expose read-only
`StateFlow`, and update immutable state with `copy`. Some older features manage
their own `MutableStateFlow`; follow the nearest established feature unless the
task explicitly includes a broader refactor.

Compose screens collect ViewModel state, render loading/loaded/failed states,
and delegate events back to the ViewModel. Shared components belong in the
design-system module; feature-specific composables remain with the feature.

## 4. Data and core logic

### Content endpoints and environments

`ProdServerConfig` constructs raw GitHub URLs under:

```text
https://raw.githubusercontent.com/TwoEightNine/LearnMokshan/master/content
```

Production lesson indexes are `lessons-{language}/index.json`; article indexes
are `articles/index-{language}.json`. Debug configuration replaces every
occurrence of `index` in these configured URLs with `index-sandbox`. Topic URLs
normally contain no `index`, so they remain production topic files after this
replacement. Phrasebook loading uses its own fixed production URL and does not
use `ServerConfig`.

Changing the repository default branch, directory layout, or filenames can
break installed clients because URLs are hard-coded.

### Network behavior

`NetworkClient` uses Ktor with:

- lenient Kotlin serialization that ignores unknown keys and explicit nulls;
- a 30-second request timeout;
- response-status exceptions (`expectSuccess = true`);
- header-level logging; and
- both Ktor `HttpCache` and an application cache abstraction.

On Android, the application cache stores response bodies in
`cacheDir/ktor_cache`, keyed by a SHA-512 hash of the URL. Creation timestamps
are held in `SharedPreferences` named `ktor_cache_meta`. The default TTL is one
hour. A fresh entry is returned immediately; after expiry, the client attempts a
network request and saves a successful response. If that request fails, an
expired cached response is returned. If neither network nor cache can provide a
response, the original request error is thrown.

### Lessons

`TopicsRepository` chooses content language from the device locale, loads topic
metadata and topic detail from the network, and combines topic metadata with
locally stored progress.

`LessonUseCase.prepareLesson`:

1. loads a topic and validates the requested lesson number;
2. selects one lesson or all topic pairs for summary/review steps;
3. shuffles phrase pairs;
4. chooses Mokshan-to-native or native-to-Mokshan direction based on lesson
   phase;
5. creates either word-bank or typed-input steps; and
6. builds word-bank choices from the correct words plus shuffled distractors,
   targeting ten choices when enough vocabulary exists.

A topic with `n` content lessons has `2n + 3` app lesson steps. The phases are
word-bank lessons, a word-bank summary, typed-input lessons, a typed-input
summary, and a full review. The repeat sentinel is `-3`; completing a repeat
does not change progress.

Answers are checked case-insensitively after removing `?`, `!`, `.`, `,`, the
en dash, and one occurrence of doubled spaces. An incorrect exercise is moved
to the end of the current lesson. Translation hints combine explicit topic
translations with translations inferred from lesson pairs.

### Progress

Lesson progress is stored in DataStore file `default.preferences_pb` under the
string key `progress`. Its serialized form is:

```text
<ongoing-topic-id>_<ongoing-lesson-number>
```

Missing progress defaults to `1_0`. Every positive topic ID below the ongoing
topic is considered complete. Completing a topic's final lesson advances to the
next topic at lesson zero; otherwise the current topic ID and completed lesson
number are stored.

There is no migration or per-profile progress model. Changing this encoding
requires an explicit compatibility plan.

### Learned-word dictionary

Completing a non-repeat lesson may add that lesson's `dictionary` entries to the
Room database `main.db`. `DictionaryRepository` resolves native translations
from topic lesson pairs, lowercases both sides, and records the current
timestamp. Existing Mokshan entries merge and deduplicate translations.

The Room 3 database is version 1 and contains `DictionaryEntity`. Lists of native
translations are serialized into a single `|`-delimited column. A schema or
converter change requires a Room migration and updated generated schemas.

`DictionaryRepository.addNewWords` currently performs per-entry insert/update
work and then calls `insertMany` for all entries. Treat this as current behavior,
not a recommended duplication pattern.

### Articles

The article index is localized and sorted by descending `added` value; article
categories are sorted alphabetically. Index URLs are resolved relative to the
article content directory.

`JartRepository` chooses a parser by file extension:

- `.json` uses the serialized JART schema;
- every other suffix uses the line-oriented Prelinar parser.

Supported JART entries are title, `h1`, `h2`, `h3`, body, hint, list, sublist,
image, and table. Unknown JSON types become `Unknown` entries. Prelinar begins
with `key: value` metadata, requires a line containing exactly `CONTENT`, and
then uses `<type>` prefixes. Prelinar images use `link | footer`; tables use
`<table>`, `<row>`, and `<endtable>` records.

### Phrasebook

Phrasebook JSON is converted into Mokshan phrases, translated categories, and
English/Russian translations. `PhrasebookUseCase` filters it to the device
locale and falls back to English. The data and feature module are present, but
the feature is not reachable from current app navigation.

## 5. Content maintenance

Content changes are production changes because released clients read these
files from GitHub.

### Lessons

Each `content/lessons-{en,ru}/` directory contains:

- `index.json`: production topic listing;
- `index-sandbox.json`: debug topic listing; and
- `<topic-id>/lessons.json`: topic lesson data.

An index topic contains `id`, `lessonsCount`, `title`, `description`, optional
`emoji`, and optional `grammar`. Topic detail contains `id`, `title`,
`description`, `version`, ordered lessons, and word translations. Each lesson
contains `order`, `toNative` phrase pairs, and dictionary words.

Maintain these invariants:

- Topic IDs, order, and lesson counts must agree between index and topic data.
- English and Russian trees must retain equivalent topic structure unless a
  deliberate staged rollout is documented.
- Every Mokshan word needed for lesson hints must appear in explicit
  translations or lesson dictionary data.
- Grammar filenames referenced by indexes must exist in `content/articles/`.
- Update sandbox indexes deliberately; do not assume they mirror production.

### Articles

`content/articles/index-{en,ru}.json` lists categories and articles. Each article
has a title, description, category list, relative URL, and ISO-style `added`
date. Sandbox indexes are separate files.

Article bodies may be JART JSON or `.prelinar`. Keep localized article pairs and
their index entries aligned. Image paths are relative to the article directory.
The Prelinar parser is strict about the `CONTENT` marker and metadata format;
malformed metadata or image/table records can fail at runtime.

### Phrasebook and legal files

`content/phrasebook/phrasebook.json` contains translated categories and phrases
for `en` and `ru`. Category IDs referenced by phrases must exist.

`content/legal/` contains English and Russian privacy-policy (`pp`) and
terms-of-service (`tos`) sources in Markdown and JSON. App-info links currently
point to raw files on the `master` branch. Maintain language and format pairs
together.

## 6. Code conventions

### Kotlin and modules

- Use the official Kotlin style configured by `kotlin.code.style=official`.
- Keep common behavior in `shared/commonMain`; use platform source sets only for
  APIs that cannot be shared.
- Keep dependency versions and plugin versions in `gradle/libs.versions.toml`.
- Add modules explicitly to `settings.gradle.kts` and respect the existing
  `android/app`, `android/core`, and `android/features` organization.
- Prefer constructor injection. Register bindings in the owning Koin module.
- Return `Result<T>` at repository/use-case boundaries where the surrounding
  code already models recoverable failure that way.
- Preserve nullable-response validation and throw a descriptive
  `InvalidEntityException` or equivalent at malformed external-data boundaries.

### Android UI

- Keep feature state immutable and expose it as read-only `StateFlow`.
- Perform asynchronous feature work in `viewModelScope`.
- Pass navigation behavior into `ScreenFactory`; do not couple feature screens
  directly to the top-level navigation controller.
- Reuse design-system components and theme values before adding local copies.
- Put user-visible strings in resources and update both default English and
  `values-ru` resources.
- Add or update Compose previews for meaningful reusable or screen-level UI.
- Keep Android-only utilities out of `:shared` common source sets.

### Naming and visibility

Follow the closest correct package convention:
`global.msnthrp.mokshan...` for shared and most Android code, with
`global.msnthrp.learnmokshan` reserved for the Android application namespace.
Do not copy the existing `learmokshan` typo or the duplicate core-design-system
namespaces into new code. Use `internal` for implementation details that do not
need to cross module boundaries.

### Documentation contract

Update `README.md` when setup, platform status, or user-visible features change.
Update this document when architecture, data formats, core behavior, build
requirements, or conventions change. Update `AGENTS.md` when the required
maintenance workflow changes.

## 7. Build, configuration, and validation

### Toolchain

The version catalog specifies Android Gradle Plugin 9.4.1 and Kotlin 2.4.21.
The checked-in wrapper uses Gradle 9.6. Android requires JDK 17 and SDK Build
Tools 36, uses compile/target SDK 36 and minimum SDK 26, and emits Java/Kotlin
JVM 17 bytecode. Android modules use AGP's built-in Kotlin support.

### Local and signing configuration

`local.properties` is used only for local Android SDK discovery. Release signing
uses all four `mokshan.release.*` Gradle properties or the matching
`MOKSHAN_RELEASE_*` environment variables. Complete values create a signed
release, no values create an unsigned release, and partial values fail release
tasks without blocking debug configuration.

Never commit `local.properties`, keystores, signing credentials, IDE-local
paths, or generated signing artifacts. The repository ignores `local.properties`
and filenames starting with `keystore-`.

### Useful checks

```shell
./gradlew :shared:allTests
./gradlew :android:app:lintDebug
./gradlew :android:app:assembleDebug
./gradlew :android:app:assembleRelease
```

Android instrumentation tests require an emulator/device and should be run
separately. They cover the Room 3 read/write and version-1 database compatibility
paths; `ServerConfigTests` asserts that its resolved indexes are not sandbox
indexes.

`shared/src/commonTest/kotlin/content/ContentTests.kt` traverses lesson files and
checks translation-hint coverage. Content work must run this test when the
toolchain is usable. If a required command cannot run, report the exact blocker
and do not claim the check passed.

## 8. Known limitations and technical debt

- iOS is a standalone placeholder and has no product feature or shared-module
  integration.
- Phrasebook and “I Speak” modules are present but unreachable.
- The dictionary package path still contains the historical `learmokshan` typo.
- There is no checked-in CI workflow, formatter/linter policy beyond Kotlin's
  official style, or automated Markdown validation.
- Production content URLs depend on the `master` branch name and GitHub raw-file
  availability.
- Network logs include response headers and several ViewModels print data or
  stack traces directly.
- The repository has no standalone license file.

These items are documentation, not authorization to fix unrelated technical
debt during a focused change.
