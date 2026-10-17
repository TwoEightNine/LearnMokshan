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
- `shared/src/commonTest`: business-logic unit tests, in-memory fakes
  (`testutil/`), and content validation with the production parsers
  (`content/`).
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

Free text that is put into a route (topic title and description, article title and
URL) must go through `encodeRouteArgument` from `:android:core:navigation`, which
percent-encodes it (spaces as `%20`) so characters such as `&`, `=`, `#`, `%`, `?`
and `{` cannot break the route; the navigation library decodes it when the
argument is read. Route patterns with `{placeholders}` are built without encoding.

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
are `articles/index-{language}.json`. Debug configuration rewrites only the file
name of the two index URLs (`index.json` -> `index-sandbox.json`, `index-en.json`
-> `index-sandbox-en.json`; the rewrite is idempotent and leaves hosts and
directories alone). Topic URLs and the articles directory are never rewritten, so
debug builds still read production topic files. Every content URL the client uses
comes from `ServerConfig`: besides the indexes and topics it provides the articles
directory (grammar articles are `directory + file name from the index`), the
per-language pronunciation article, and the phrasebook file (no sandbox variant).
Nothing else in the Android code builds content URLs. A unit test checks that every
URL of `ProdServerConfig` (and the sandbox indexes) points to an existing file in
`content/`.

Changing the repository default branch, directory layout, or filenames can
break installed clients because URLs are hard-coded.

### Network behavior

`NetworkClient` uses Ktor with:

- lenient Kotlin serialization that ignores unknown keys and explicit nulls;
- a 30-second request timeout;
- response-status exceptions (`expectSuccess = true`);
- header-level logging only when the Koin property `HTTP_LOGGING_PROPERTY` is
  `true` (the Android application sets it to `BuildConfig.DEBUG`); and
- both Ktor `HttpCache` and an application cache abstraction.

`NetworkClient` reads the cache and parses bodies on `Dispatchers.Default`, so
callers on the main thread are never blocked by disk access or JSON/Prelinar
parsing.

On Android, the application cache stores response bodies in
`cacheDir/ktor_cache`, keyed by a SHA-512 hash of the URL, and reads and writes
them on `Dispatchers.IO`. Bodies already read or written in the current process
are also kept in a bounded in-memory LRU, so screens that reload on resume do not
touch the disk again. Creation timestamps are held in `SharedPreferences` named
`ktor_cache_meta`. The default TTL is one hour. A fresh entry is returned
immediately; after expiry, the client attempts a network request. A response body
is cached and returned only after it has been parsed (decoded and, for the data
sources, mapped to the domain model) successfully, so an error page or malformed
file never replaces a good cached copy, and an unparsable cached entry is ignored.
An expired cached copy is parsed only when it is needed as a fallback. If the
request fails or its body cannot be parsed, an expired cached copy that parses is
returned. If the request has not answered within three seconds
(`NetworkClient.STALE_FALLBACK_TIMEOUT_MS`) and an expired copy parses, that copy is
returned at once while the request keeps running in the background and updates the
cache for the next load; without a usable copy the client keeps waiting for the
request. If nothing is usable, the request error (or the parse error) is thrown.
Coroutine cancellation is never turned into a cache fallback.

### Lessons

`TopicsRepository` chooses content language from the device locale, loads topic
metadata and topic detail from the network, and combines topic metadata with
locally stored progress.

`LessonUseCase.prepareLesson`:

1. loads a topic and validates the requested lesson number (`1..2n + 3`, or the
   repeat sentinel; `0` is rejected);
2. selects one lesson or all topic pairs for summary/review steps (lesson `k` is
   the `k`-th lesson when lessons are sorted by `order`, both when a lesson is
   played and when it is completed);
3. shuffles phrase pairs;
4. chooses Mokshan-to-native or native-to-Mokshan direction based on lesson
   phase;
5. creates either word-bank or typed-input steps; and
6. builds word-bank choices from the correct words plus shuffled distractors,
   targeting ten choices when enough vocabulary exists.

`LessonUseCase` takes a `kotlin.random.Random` (default `Random.Default`) so the
shuffling can be made deterministic in tests; `useCaseModule` registers it
explicitly instead of through `singleOf`.

A topic with `n` content lessons has `2n + 3` app lesson steps. The phases are
word-bank lessons, a word-bank summary, typed-input lessons, a typed-input
summary, and a full review. The repeat sentinel is `-3`; completing a repeat
does not change progress.

Texts are normalised by lowercasing, removing `?`, `!`, `.`, `,` and the en dash,
and collapsing any whitespace (including non-breaking spaces) to single spaces;
this form is used for word-bank words, hints, and dictionary lookups. Answers are
compared more leniently: additionally apostrophes (including typographic ones),
quotes, ellipsis, colons, and the em dash are ignored, hyphens count as spaces,
and `ё` equals `е` (so `все` is also accepted for `всё`). An empty answer is never
correct. A word bank is always shuffled, and a
lesson pair without any native translation is skipped when a lesson is prepared.
If a lesson cannot be prepared, the lesson screen shows the shared failed view with
a retry action. `LessonViewModel` ignores a repeated "check" while a result sheet is shown and a
word-bank chip that is already selected. An incorrect exercise is moved to the end
of the current lesson.
Translation hints combine explicit topic translations with translations inferred
from lesson pairs.

### Progress

Lesson progress is stored in DataStore file `default.preferences_pb` under the
string key `progress`. Its serialized form is:

```text
<ongoing-topic-id>_<ongoing-lesson-number>
```

Missing or unreadable progress defaults to `1_0` (a value whose first two
`_`-separated parts are not a topic ID >= 1 and a lesson number >= 0 is unreadable;
extra parts are ignored), and a corrupt DataStore file is replaced with an empty
one, so the lessons are never locked by bad data. Every positive topic ID below
the ongoing topic is considered complete. Completing a topic's final lesson
advances to the next topic at lesson zero; otherwise the current topic ID and
completed lesson number are stored. Progress only moves forward: completing an
earlier lesson again leaves the stored progress unchanged.

`LessonUseCase.completeLesson` saves the lesson's dictionary words first and the
progress second, and returns the first failure. Saving words is idempotent, so a
failed completion can be retried without losing words. `LessonViewModel` shows the
lesson as completed only after both succeed; otherwise the lesson screen shows the
shared failed view with a retry action.

There is no migration or per-profile progress model. Changing this encoding
requires an explicit compatibility plan.

### Learned-word dictionary

Completing a non-repeat lesson may add that lesson's `dictionary` words to the
Room database `main.db`. The entries are exactly the words listed in the lesson's
`dictionary`. `LessonUseCase` takes their translations from the topic's
`translations` (matched ignoring case and the punctuation removed by answer
checking); a word with no `translations` entry falls back to the lesson pair with
the same text, which is how whole phrases get a translation. A word that has
neither is skipped. Both sides are lowercased, and `DictionaryRepository` records
the current timestamp. Existing Mokshan entries merge and deduplicate
translations.

The Room 3 database is version 1 and contains `DictionaryEntity`. Lists of native
translations are serialized into a single `|`-delimited column; a `|` or `\`
inside a translation is escaped with a backslash, an empty list is an empty string
(so a list holding only an empty string is read back as empty), and rows written
before escaping existed are read unchanged. A schema or converter change requires
a Room migration and updated generated schemas.

`DictionaryRepository` receives a `DictionaryDao` (obtained from `AppDatabase` in
`repositoryModule`) so it can be tested without Room.

If the dictionary cannot be loaded and no words are shown yet, the dictionary screen
shows the shared failed view with a retry action instead of the "no words yet"
text; words that are already shown stay on screen when a refresh fails.

`DictionaryRepository.addNewWords` writes each word once: it inserts an unknown
word, or merges the new translations into the existing row and refreshes its
timestamp. Repeating the call with the same entries does not add rows. The table
has no unique index on `mokshan`, so databases written by older versions may still
contain duplicate rows; `getAllWords` shows one entry per word.

### Articles

The article index is localized and sorted by descending `added` value; article
categories are sorted alphabetically. Index URLs are resolved relative to the
article content directory.

`JartRepository` chooses a parser by file extension:

- `.json` uses the serialized JART schema;
- every other suffix uses the line-oriented Prelinar parser.

Supported JART entries are title, `h1`, `h2`, `h3`, body, hint, list, sublist,
image, and table. Unknown JSON types become `Unknown` entries. JSON tables need a
`size` of exactly `[columns, rows]` with positive numbers and at least one cell,
otherwise the entry is dropped. Prelinar begins with `key: value` metadata
(blank lines are skipped, a value may contain `: `, `version` is required and
numeric), requires a line containing exactly `CONTENT`, and then uses `<type>`
prefixes. A line is a prefix line only if it starts with `<name>` followed by the
end of the line or one space; any other line (for example `<b>bold</b>` or
`<h1>glued`) is body text. Blank lines become empty body entries, which the
renderer uses as spacing. `\n`, `\r\n` and `\r` line endings are accepted. Prelinar
images use `link | footer` (the footer is optional and may contain `|`); tables use
`<table>`, `<row>`, and `<endtable>` records, short rows are padded to the widest
row, an empty table is skipped, and a table left open at the end of the file keeps
its rows.

### Phrasebook

Phrasebook JSON is converted into Mokshan phrases, translated categories, and
English/Russian translations. `PhrasebookUseCase` returns phrases grouped by
category in the order of the categories, with texts in the device language; a
category or a phrase that has no translation into it is shown in English. A
category without any usable phrase, a category without any usable translation
(with its phrases), and a phrase without any usable translation are left out;
duplicated category IDs keep the first one. If nothing is left, loading fails with
"Phrasebook is empty". The data and feature module are present, but the feature
is not reachable from current app navigation.

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

The client does not repair content: an unusable lesson (no `order`, no usable
pairs), lessons sharing an `order`, a topic file whose `id` differs from the
requested one, an unusable index entry (no `id`/`title`, `lessonsCount` below 1),
or duplicated topic ids reject the whole file. Such a file is never cached, so
the last good cached copy keeps being used. A lesson without `dictionary` is
valid and has no dictionary words; single pairs or translations with missing
fields are skipped.

Maintain these invariants:

- `topicsCount` in every `index*.json` must equal the number of listed topics.
- Topic IDs, order, and lesson counts must agree between index and topic data:
  `lessonsCount` in every `index*.json` must equal the number of lessons in the
  topic's `lessons.json` (enforced by `ContentContractTests`).
- English and Russian trees must retain equivalent topic structure unless a
  deliberate staged rollout is documented.
- Every Mokshan word needed for lesson hints must appear in explicit
  translations or lesson dictionary data.
- Every word in a lesson's `dictionary` must be translatable: it needs a
  `translations` entry or a lesson pair with the same text (otherwise it is never
  added to the user's dictionary).
- The Mokshan side (pair texts, lesson `dictionary`, `translations` keys) must be
  identical in the English and Russian trees; only the native texts differ.
- Grammar filenames referenced by indexes must exist in `content/articles/`.
- Update sandbox indexes deliberately; do not assume they mirror production.

### Articles

`content/articles/index-{en,ru}.json` lists categories and articles. Each article
has a title, description, category list, relative URL, and ISO-style `added`
date. An index without an `articles` list, or whose articles are all unusable, is
rejected (not cached, last good copy kept) instead of showing an empty list; an
empty `articles` list is valid, a missing `categories` list is tolerated. Sandbox indexes are separate files. `added` (`yyyy-MM-dd`) is parsed
strictly and locale-independently as a calendar date in UTC (an invalid date
becomes `0` and sorts last); the article list formats it back in UTC with the
device locale's month names, so the displayed day never depends on the time zone.

Article bodies may be JART JSON or `.prelinar`. Keep localized article pairs and
their index entries aligned. Image paths are relative to the article directory.
The Prelinar parser is strict about the `CONTENT` marker and the `key: value`
metadata format, and requires a numeric `version`; a malformed file makes the
article fail to load (and is not cached).

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
  code already models recoverable failure that way. Wrap suspending work with
  `runCatchingCancellable`, not `runCatching`: it rethrows `CancellationException`
  so a cancelled coroutine is not reported as a failed result.
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

### Unit tests

- `./gradlew :shared:allTests` runs `shared/src/commonTest` on the Android host
  target. It covers `LessonUseCase`, the repositories, `NetworkClient` (Ktor mock
  engine plus a fake cache), `LessonsStorageDataSource` (a real DataStore in a
  temporary directory), the Prelinar/JART/lesson/article/phrasebook mappers,
  `PhrasebookUseCase`, the Koin graph, and `ContentContractTests`/`ContentTests`
  over `content/`.
- `./gradlew testDebugUnitTest` runs the JVM tests of the Android modules
  (`LessonViewModel`, the list/article/dictionary/phrasebook view models,
  `BaseViewModel`, route builders, server configs, locale mapping). View models
  are tested with a `MainDispatcherRule` and real use cases over in-memory fakes;
  view models that use `KoinJavaComponent.inject` start and stop Koin in the test.
- Not covered by unit tests: `AndroidCacheStorage` (needs a `Context`), real Room
  queries, and Compose UI.

Convention for known defects: a test whose name contains `knownBug`/`knownIssue`
pins behaviour that is wrong today, so the suite stays green; it is expected to
fail when the defect is fixed and must be updated together with the fix. There are
currently no such tests. Never weaken a test to make it pass without fixing the
behaviour.

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
- The repository has no standalone license file.

These items are documentation, not authorization to fix unrelated technical
debt during a focused change.
