# Sincely — code guide

Where things live, how data flows from the database to the screen, and in
what order to touch files when adding something yourself. Written from the
actual state of the repo, not from planning docs.

## Table of contents

1. [Modules and layers](#1-modules-and-layers)
2. [Domain layer](#2-domain-layer)
3. [Database (SQLDelight)](#3-database-sqldelight)
4. [Repository](#4-repository)
5. [Koin (DI)](#5-koin-di)
6. [Android UI](#6-android-ui)
7. [iOS — current state](#7-ios--current-state)
8. [Build and tooling](#8-build-and-tooling)
9. [Cookbook: adding a new feature](#9-cookbook-adding-a-new-feature)

## 1. Modules and layers

Kotlin Multiplatform: all logic and the database live in one shared module,
and each platform only adds a screen layer on top.

| Module | Responsibility |
|---|---|
| `shared` | Domain models, the SQLDelight database, the repository, common Koin wiring. All business logic lives here — both platforms consume it. |
| `androidApp` | Single activity, full UI in Jetpack Compose + Material 3. Fully working app. |
| `iosApp` | Separate Xcode project (SwiftUI), links `shared` as a framework. Currently a stub — see [section 7](#7-ios--current-state). |

### How data flows from the database to the screen

```
UI
  androidApp/.../ui/*.kt (Compose)  ·  iosApp/iosApp/*.swift (SwiftUI)
        │
        ▾
Screen state
  TrackerListViewModel.kt (Android)  ·  IosTrackerGateway.kt (iOS)
        │
        ▾
Repository (interface)
  repository/TrackerRepository.kt
        │
        ▾
Repository (implementation)
  SqlDelightTrackerRepository.kt  ·  EntityMappers.kt
        │
        ▾
Database
  Tracker.sq  ·  CheckIn.sq  ·  DatabaseDriverFactory (expect/actual)
```

Every layer from the ViewModel down is wired together by **Koin** — nothing
is constructed by hand at the call site. Details in [section 5](#5-koin-di).

## 2. Domain layer

Everything lives in
`shared/src/commonMain/kotlin/app/sincely/shared/domain/`. Two real
entities, the rest is supporting logic around them.

### `Tracker` — `domain/Tracker.kt`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | database key |
| `name`, `emoji` | `String` | e.g. "Water the monstera", 🌿 |
| `targetDays` | `Int?` | expected interval in days — `null` means no cadence, never goes to warning/overdue |
| `category` | `TrackerCategory` | `DOM` / `AUTO` / `ZDROWIE` / `SERWER` / `INNE` / `CUSTOM` |
| `reminderEnabled`, `reminderTime` | `Boolean`, `ReminderTime` | `RANO` (morning) / `WIECZOREM` (evening) |
| `createdAt`, `archivedAt` | `Instant`, `Instant?` | archiving is a soft delete — the row stays in the database |

### `CheckIn` — `domain/CheckIn.kt`

| Field | Type | Notes |
|---|---|---|
| `id`, `trackerId` | `Long` | a single "I did the thing" event for a tracker |
| `timestamp` | `Instant` | when it actually happened (can be backdated) |
| `note` | `String?` | optional note |
| `backdated` | `Boolean` | `true` when logged for a date earlier than "now" |

**Type note:** every `Instant` here is `kotlin.time.Instant` (the newer
stdlib type), not `kotlinx.datetime.Instant`. Hence the custom serializer in
`InstantSerializer.kt` — `InstantIso8601Serializer` from kotlinx-datetime
doesn't resolve on Kotlin/Native targets.

### Status is never stored

`domain/StatusCalculator.kt` computes a tracker's status on the fly from its
last check-in and `targetDays` — none of it is persisted:

- `OK`
- `WARNING`
- `OVERDUE`

The warning threshold is half of `targetDays` (minimum 1 day, rounded up).
Other supporting logic in the same directory:

- `TrackerStats.kt` — `computeTrackerStats()`: average and longest gap, plus
  up to 10 chart bars; needs at least 2 check-ins, otherwise returns `null`.
- `TrackerCategory.kt` — the category enum plus `TrackerCategoryPresentation`
  (label/emoji, currently in Polish).
- `PopularTrackerSuggestion.kt` — 5 ready-made suggestions (water plants,
  replace filter, backup, oil change, bedsheets) for the empty list state.
- `RelativeTimeFormatter.kt` / `HistoryDateFormatter.kt` — "today" /
  "yesterday" / "N days ago" vs. an absolute date.

## 3. Database (SQLDelight)

Schema and queries are written as `.sq` files; SQLDelight generates typed
Kotlin from them. Two tables, under
`shared/src/commonMain/sqldelight/app/sincely/shared/db/`.

```sql
-- Tracker.sq (abridged)
CREATE TABLE trackerEntity (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  emoji TEXT NOT NULL,
  targetDays INTEGER,
  category TEXT AS TrackerCategory NOT NULL,
  customCategoryLabel TEXT,
  reminderEnabled INTEGER AS Boolean NOT NULL,
  reminderTime TEXT AS ReminderTime NOT NULL,
  createdAt INTEGER AS Instant NOT NULL,
  archivedAt INTEGER AS Instant
);
-- selectAll: WHERE archivedAt IS NULL ORDER BY createdAt DESC

-- CheckIn.sq (abridged)
CREATE TABLE checkInEntity (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  trackerId INTEGER NOT NULL REFERENCES trackerEntity(id),
  timestamp INTEGER AS Instant NOT NULL,
  note TEXT,
  backdated INTEGER AS Boolean NOT NULL
);
-- selectLastCheckInPerTracker: GROUP BY trackerId, MAX(timestamp)
--   (avoids an N+1 Flow fan-out when sorting the list)
```

Columns typed `AS TrackerCategory` / `AS Instant` / `AS Boolean` need
adapters — these live in `db/DatabaseAdapters.kt`: `instantAdapter` (Instant
↔ epoch millis), `trackerCategoryAdapter`, `reminderTimeAdapter` (enum ↔
String). The same file has `createDatabase(driver)`, which wires all the
adapters into one `SincelyDatabase` instance.

The driver itself (`SqlDriver`) is `expect`/`actual`:
`db/DatabaseDriverFactory.kt` declares the contract, and each platform
supplies the implementation — `AndroidSqliteDriver` on Android,
`NativeSqliteDriver` on iOS. The database file on disk is `sincely.db`.

## 4. Repository

One interface, one implementation, no use-case layer in between it and the
ViewModel — business rules (status, stats) are plain functions in
`domain/`, called directly.

```kotlin
interface TrackerRepository {
    fun observeTrackers(): Flow<List<Tracker>>
    suspend fun addTracker(...): Long
    suspend fun updateTracker(...)
    suspend fun archiveTracker(id, archivedAt: Instant)
    suspend fun deleteTracker(id)

    suspend fun checkIn(trackerId, timestamp, note, backdated): Long
    suspend fun undoLastCheckIn(trackerId)
    suspend fun lastCheckIn(trackerId): CheckIn?
    fun observeCheckIns(trackerId): Flow<List<CheckIn>>
    fun observeLastCheckIns(): Flow<Map<Long, Instant>>
}
```

`repository/TrackerRepository.kt` is the contract.
`repository/SqlDelightTrackerRepository.kt` is the implementation:
`observeTrackers`/`observeCheckIns`/`observeLastCheckIns` use
`.asFlow().mapToList()`, while `addTracker`/`checkIn` wrap the insert in
`database.transactionWithResult { ... lastInsertRowId() }` to atomically get
the new id back. Row-to-domain mapping lives in
`repository/EntityMappers.kt`.

## 5. Koin (DI)

Four Koin modules, no manual `new`-ing of the repository anywhere in screen
code.

| Module | File | Provides |
|---|---|---|
| `sharedModule` | `shared/.../di/SharedModule.kt` | `SqlDriver`, `SincelyDatabase`, `TrackerRepository` |
| `platformModule()` (Android) | `di/PlatformModule.android.kt` | `DatabaseDriverFactory(androidContext())` |
| `platformModule()` (iOS) | `di/PlatformModule.ios.kt` | `DatabaseDriverFactory()` |
| `androidAppModule` | `androidApp/.../di/AndroidAppModule.kt` | `viewModel { TrackerListViewModel(get()) }` |

Koin startup — on Android, in `SincelyApplication.onCreate()`:
`startKoin { modules(sharedModule, platformModule(), androidAppModule) }`.
On iOS, in `KoinHelper.doInitKoin()`, called from `iosAppApp.swift`'s
`init()`. Compose screens get the ViewModel via `koinViewModel()` in
`ui/SincelyApp.kt`.

## 6. Android UI

Everything under `androidApp/src/main/kotlin/app/sincely/android/`. One
activity, one large ViewModel holding all screen state, hand-rolled
navigation (no Compose Navigation).

### Navigation without a library

There's no Voyager or Compose Navigation. `Screen` (a sealed interface:
`List` / `Detail(trackerId)`) sits as a `StateFlow` on the ViewModel, and
`ui/SincelyApp.kt` switches on it with a `when`. The system back button is
intercepted in the same file with
`BackHandler(enabled = screen is Screen.Detail) { viewModel.backToList() }`.

### Screen files

| File | Role |
|---|---|
| `TrackerListScreen.kt` | list, category filter chips, FAB, both bottom sheets, undo toast |
| `TrackerDetailScreen.kt` | tracker detail: header, stats card, inline edit, history, archive/delete |
| `AddTrackerSheet.kt` | bottom sheet for creating a tracker + popular-suggestions list |
| `CheckInOptionsSheet.kt` | "check in with options" bottom sheet — backdated date + note |
| `TrackerCard.kt` | list row: tap = quick check-in, long-press = options sheet, arrow = detail |
| `TrackerFormFields.kt` | shared form rows (name, emoji, category, interval, reminder) — used by both Add and Detail |
| `TrackerPresentation.kt` | pure functions: `Tracker` + last check-in + "now" → UI-ready status/color/text |
| `SceneHeader.kt` | day/night banner above the list; tap toggles the theme |
| `AndroidStrings.kt` | all UI copy as constants, kept out of composables |
| `theme/Theme.kt` | `SincelyColors` (a custom palette, not Material roles) + `LocalSincelyColors` |

### `TrackerListViewModel` — function map

One ViewModel, injected only with `TrackerRepository`. Its public API,
grouped by what it handles:

- **Navigation / theme** — `openDetail()`, `backToList()`, `toggleTheme()`,
  `setFilterCategoryLabel()`
- **Quick check-in** — `quickCheckIn()` (writes + shows an auto-dismissing
  toast), `undoToast()` (undo from the toast action)
- **"Check-in with options" sheet** — `openCheckInSheet()`,
  `setCheckInDateChoice()`, `updateCheckInNote()`,
  `submitCheckInWithOptions()`
- **Add-tracker sheet** — `updateDraftName()`, `pickDraftEmoji()`,
  `pickDraftCategory()`, `toggleDraftInterval()`, `incrementDraftInterval()`
  / `decrementDraftInterval()`, `submitAdd()`, `addFromSuggestion()`
- **Detail-screen editing** — `mutateTracker()` (private helper: read →
  transform → save), plus the full set of `updateDetail*`/`pickDetail*`/
  `toggleDetail*`, `undoLastCheckIn()`, `archiveTracker()`,
  `deleteTracker()`

If you want to understand this file quickly, read it in this order: state
→ navigation → quick check-in → add sheet → detail editing — that's roughly
how it's laid out physically.

## 7. iOS — current state

> **Stale:** `iosApp/iosApp/TrackerListView.swift` calls
> `gateway.getTrackers()` and `gateway.addSampleTracker()`, but
> `IosTrackerGateway.kt` (in `shared/src/iosMain`) has long since moved to a
> richer API (`getTrackerCards()`, `getDetail()`, `quickCheckIn()`,
> `checkInWithOptions()`, DTOs like `TrackerCardData`/`TrackerDetailData`).
> This SwiftUI file most likely no longer compiles against the current
> `shared` framework.

In other words: all the unglamorous work (status computation, date
formatting, flattening everything into DTOs Swift can consume) is already
done in `IosTrackerGateway.kt` — what's missing is the actual SwiftUI
screens (the counterparts to `TrackerListScreen`/`TrackerDetailScreen`/the
sheets/the theme on Android). `notes/features.md` confirms this — the iOS
section is mostly unchecked TODOs.

## 8. Build and tooling

- **Versions** (`gradle/libs.versions.toml`) — Kotlin 2.3.21 (K2), AGP
  8.13.2, minSdk 26 / compile-target 36, SQLDelight 2.3.2, Koin 4.2.2,
  coroutines 1.11.0, kotlinx-datetime 0.8.0, Compose BOM 2025.09.01.
- **Convenience Gradle tasks** (root `build.gradle.kts`) — `buildApk`,
  `startEmulatorIfNeeded` (boots the `sincely` AVD if nothing is connected),
  `runOnEmulator` (build + install + launch, depends on the previous one).
- **Modules in `settings.gradle.kts`** — only `:shared` and `:androidApp`.
  iOS isn't a Gradle module — Xcode calls
  `./gradlew :shared:embedAndSignAppleFrameworkForXcode` as a build phase.
- **CI** (`.github/workflows/ci.yml`) — `:shared:allTests` +
  `:androidApp:assembleDebug`. No iOS/Xcode job yet.
- **Domain tests** — `shared/src/commonTest/kotlin/.../domain/`, pure JVM
  tests of status/stats/formatting logic, no Android or simulator needed.

## 9. Cookbook: adding a new feature

Worked example: "add a priority field to trackers (low/medium/high)". The
order below is the real dependency order, not a suggestion — skipping ahead
usually just gets you a compile error one layer up.

1. **Domain** — `domain/Tracker.kt`: new field on `data class Tracker`. If
   it's an enum like category, a separate `TrackerPriority.kt` file plus a
   `*Presentation` object, following `TrackerCategoryPresentation`.
2. **Database** — `Tracker.sq`: new column on `trackerEntity`, update
   `insert`/`update`. An enum needs an adapter in `DatabaseAdapters.kt`,
   same pattern as `trackerCategoryAdapter`.
3. **Mapping** — `EntityMappers.kt`: add the field in
   `TrackerEntity.toDomain()`.
4. **Repository** — `TrackerRepository.kt`: new parameter on
   `addTracker`/`updateTracker`. `SqlDelightTrackerRepository.kt`: pass it
   through to the query.
5. **ViewModel** — `TrackerListViewModel.kt`: a field on `TrackerDraft`
   (adding) plus a `pickDraftPriority()` function; the same on the edit
   side (`pickDetailPriority()`, through `mutateTracker()`).
6. **UI** — a new form row in `TrackerFormFields.kt` (following
   `CategorySection`), wired into `AddTrackerSheet.kt` and
   `TrackerDetailScreen.kt`. If priority should show on the list, also
   `TrackerPresentation.kt` and `TrackerCard.kt`.
7. **iOS (optional, for now)** — since the iOS screens need a rewrite
   anyway ([section 7](#7-ios--current-state)), updating
   `IosTrackerGateway.kt` can wait until someone actually picks up the
   SwiftUI work.
8. **Verify** — `./gradlew :shared:allTests` (if you added logic worth
   testing in `domain/`), then `./gradlew :androidApp:installDebug` on the
   emulator.

### If you're reading the code from scratch

Best reading order: `TrackerListViewModel.kt` (what the app actually does)
→ `SincelyApp.kt` + `TrackerListScreen.kt` (how it renders) → `domain/`
(where status and stats come from) → `TrackerRepository.kt` /
`SqlDelightTrackerRepository.kt` (how it reaches the database). Leave DI
(`di/*.kt`) for last — it's just wiring, not logic.
