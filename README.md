# memrica-android-v2

A Kotlin and Jetpack Compose rewrite of the 504 vocabulary app (`app.enjoylearning504`). It keeps
the original app's learning logic, screens and user lifecycle, rebuilt with clean architecture.

## Stack

- Kotlin 2.4, AGP 9.5, Gradle 9.8, compile and target SDK 37, min SDK 23
- Jetpack Compose (Material 3) and type-safe Navigation Compose
- Ktor client for networking, Koin for dependency injection
- Room (two databases), DataStore, Media3 ExoPlayer, Tapsell mediation ads

## Modules

| Module | What it holds |
| --- | --- |
| `app` | `MainActivity`, `MemricaApplication` (Koin), navigation, splash and start-up gate |
| `core:model` | Plain Kotlin models |
| `core:domain` | Repository interfaces, use cases, `LearningSessionEngine`, `QuizBuilder`, `AdTurnPolicy`, `ProgressCalculator` |
| `core:data` | Repository implementations, `LegacyDatabaseMigrator`, connectivity |
| `core:database` | `ContentDatabase` (prebuilt, read-only) and `UserDatabase` (progress) |
| `core:datastore` | App flags and Keystore-encrypted auth tokens |
| `core:network` | Ktor clients: Memrica auth backend and the original `conf.json` |
| `core:audio` | Word recordings (ExoPlayer) and sentence text-to-speech |
| `core:ads` | `AdsController`, Tapsell implementation, native ad composable |
| `core:designsystem`, `core:ui` | Theme, fonts, plant images, shared components |
| `feature:*` | `auth`, `home`, `lesson`, `word`, `learning`, `quiz`, `difficult`, `info` |

Features depend only on `core:domain` interfaces; `core:domain` has no Android dependency.

## Building

```bash
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```

Debug builds talk to a Memrica server on the development machine (`http://10.0.2.2:8080/` from
the emulator). Set the release URL in `core/network/build.gradle.kts`.

## Upgrading from the original app

v2 keeps the original application ID, so it installs as an update. On first launch
`LegacyDatabaseMigrator` copies word levels, difficult flags and class high scores out of the old
`databases/my504`, then keeps that file as `my504.legacy.bak`. Release builds must be signed with
the store app's keystore and use a `versionCode` above 25.

## Content database

`core/database/src/main/assets/database/content.db` is generated from the original word list:

```bash
tools/build-content-db.sh <path to original my504> core/database/src/main/assets/database/content.db
```

Bump `ContentDatabase.VERSION` when the content changes.
