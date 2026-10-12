# Instructions for Coding Agents

This file applies to the entire repository. A more deeply nested `AGENTS.md`, if
added later, may refine these rules for its subtree but must not silently weaken
security, validation, or documentation requirements.

## 1. Read before changing anything

Before editing:

1. Read `README.md` for current product status, setup, and supported platforms.
2. Read the relevant sections of `DOCS.md` for architecture, behavior, content
   formats, conventions, and known limitations.
3. Inspect `git status` and preserve all user-owned or unrelated changes.
4. Inspect the implementation and tests nearest to the requested change. Do not
   rely on documentation alone when source can answer a question.
5. Check `settings.gradle.kts`, the affected module build file, and
   `gradle/libs.versions.toml` before changing modules or dependencies.

Treat `README.md`, `DOCS.md`, and this file as a maintained contract. If a change
makes them inaccurate, update the affected documentation in the same change.

## 2. Scope and change discipline

- Make the smallest coherent change that fulfills the request.
- Do not repair documented technical debt unless it is in scope or blocks the
  requested result.
- Do not reformat, rename, or reorganize unrelated code or content.
- Do not overwrite, revert, or delete user changes. Stop and report an overlap
  that cannot be handled safely.
- Do not enable dormant Phrasebook or “I Speak” navigation, or describe those
  features as active, unless the task explicitly implements and verifies them.
- Do not claim iOS feature parity. The current iOS app is a placeholder unless a
  task demonstrably changes that state.
- Preserve backward compatibility for remotely consumed content and locally
  persisted data unless the task includes a migration plan.

## 3. Architecture rules

### Shared Kotlin

- Put cross-platform models, use cases, repositories, network behavior, and
  storage abstractions in `shared/src/commonMain`.
- Put Android/iOS APIs only in their platform source sets. Bridge platform needs
  through an interface or `expect`/`actual` declaration.
- Keep domain models independent from Android UI and framework types.
- Prefer constructor injection and register dependencies in the Koin module
  owned by the relevant layer.
- Preserve the UI-to-use-case/repository-to-data-source dependency direction.
  Do not make lower layers depend on Android features.
- Follow surrounding `Result<T>` error handling. Validate external nullable DTOs
  before constructing non-null domain objects.

### Android

- Keep application assembly, top-level navigation, platform bindings, and build
  variants in `:android:app`.
- Put reusable UI/theme elements in `:android:core:designsystem`, navigation
  primitives in `:android:core:navigation`, generic state support in
  `:android:core:arch`, and small Android utilities in `:android:core:utils`.
- Keep feature UI and state in the owning `:android:features:*` module.
- Expose immutable UI state through `StateFlow`; update data classes with
  `copy`; launch asynchronous ViewModel work in `viewModelScope`.
- Pass navigation callbacks through `ScreenFactory` and use `Router` for route
  construction. Do not introduce feature-level ownership of the root
  `NavController` without an explicit architectural change.
- Use the design system and resource strings. Any new user-visible string must
  have both default English and `values-ru` coverage.
- Add or update Compose previews when changing meaningful visual components.

### Dependencies and versions

- Define library/plugin versions and aliases in `gradle/libs.versions.toml`.
- Use type-safe project accessors and existing catalog aliases in module build
  files.
- Do not add a dependency when the existing stack already provides the needed
  capability.
- If adding or removing a module, update `settings.gradle.kts`, the architecture
  table in `DOCS.md`, and affected setup/build guidance.

## 4. Kotlin and naming conventions

- Follow official Kotlin style as configured by `kotlin.code.style=official`.
- Match naming, ordering, trailing commas, and visibility in nearby code.
- Prefer clear immutable values and small functions over shared mutable state.
- Use `internal` for implementation details that do not form a module API.
- Use `global.msnthrp.mokshan...` for shared and normal Android packages.
  `global.msnthrp.learnmokshan` is the Android app namespace; do not propagate
  the existing `learmokshan` typo or reused design-system namespaces.
- Keep comments focused on non-obvious decisions and invariants, not line-by-line
  narration.

## 5. Content rules

Files under `content/` are live application data fetched from GitHub. Treat a
content-only commit with the same care as an application release.

- Preserve the existing JSON and Prelinar wire shapes unless the client parser
  is changed compatibly in the same task.
- Maintain English/Russian parity for lesson structure, localized articles,
  phrasebook translations, and legal document variants. If parity is
  intentionally delayed, document the exact exception.
- Keep lesson topic IDs, `lessonsCount`, lesson `order`, grammar references, and
  topic directories consistent.
- Ensure Mokshan words used by lessons have translation-hint coverage through
  topic translations or lesson dictionary data.
- Keep production and `index-sandbox` changes deliberate. Debug uses sandbox
  lesson/article indexes; it does not use a separate phrasebook URL.
- Keep article index URLs relative to `content/articles/`, image references
  resolvable, and `added` dates consistently formatted.
- In Prelinar, preserve valid `key: value` metadata, the exact `CONTENT` marker,
  and supported entry/table/image syntax described in `DOCS.md`.
- Do not rename the `master` branch paths or content directories without also
  updating every hard-coded client URL and considering already released clients.

## 6. Security and repository hygiene

Never commit or expose:

- `local.properties`;
- keystores or signing certificates;
- store passwords, key passwords, aliases tied to private signing material, API
  credentials, or tokens;
- absolute developer-machine paths; or
- generated build, CocoaPods, IDE, database, or cache artifacts.

Examples in documentation and tests must use unmistakable placeholders. Do not
print secret-bearing configuration while diagnosing a build; inspect property
names without emitting their values.

Preserve `.gitignore` coverage. Do not weaken ignore rules to make a local build
work.

## 7. Validation requirements

Run checks proportional to the changed area with the checked-in Gradle 9.6
wrapper. Never report an unrun check as passing.

### Minimum checks by change type

| Change | Required validation |
| --- | --- |
| Markdown only | Check links/paths, inspect the rendered structure, run `git diff --check`, and verify no secrets or unrelated edits. |
| Shared Kotlin | Relevant shared tests, then `:shared:allTests`; compile affected Android targets. |
| Android UI/logic | Affected tests (`testDebugUnitTest`), `:android:app:lintDebug`, and `:android:app:assembleDebug`; inspect previews or runtime UI when layout changes. |
| Lessons/content | `:shared:allTests`, especially `ContentTests`; verify both language trees and relevant production/sandbox indexes. |
| Network/config | Unit/integration coverage plus debug and release URL expectations; run instrumentation tests when their environment is available. |
| Room/DataStore | Tests for read/write and migration/compatibility behavior; build all affected KMP targets. |
| iOS placeholder | Build the standalone Xcode target when available; it must not require Gradle or CocoaPods. |
| Build/dependency changes | Sync/resolve the full project and build every directly affected target; update setup/version documentation. |

Typical commands are:

```shell
./gradlew :shared:allTests
./gradlew testDebugUnitTest
./gradlew :android:app:lintDebug
./gradlew :android:app:assembleDebug
```

Release tasks produce an unsigned artifact when signing material is absent. Do
not create or substitute production credentials just to satisfy validation.

Tests named `*knownBug*`/`*knownIssue*` pin currently wrong behavior. When you fix
the behavior, update the pinning test in the same change; never edit one just to
make it pass.

If validation is blocked, report:

1. the exact command attempted;
2. the exact failure or missing prerequisite; and
3. which claims remain unverified.

## 8. Completion checklist

Before handing off a change:

- Re-read the user request and confirm every requested outcome is present.
- Review `git status` and the full diff for accidental files or unrelated edits.
- Confirm code follows the nearest project convention and layer boundary.
- Confirm English/Russian resources or content were updated together when
  applicable.
- Update `README.md`, `DOCS.md`, and/or this file when their documented contract
  changed.
- Run the applicable validations and accurately summarize results and blockers.
