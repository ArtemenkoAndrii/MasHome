# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

MasHome ("Mas Money") is a native Android app (Kotlin), package `com.mas.mobile`. It's a personal budgeting
app whose distinguishing feature is automatic expense capture: it reads incoming SMS messages and
notifications from banking/payment apps, matches them against user-defined patterns/templates, extracts an
amount and merchant, and turns them into spending entries against the active budget.

## Build & test

Standard Gradle Android project (single module: `app`). Use the wrapper.

```
./gradlew assembleDebug              # build debug APK
./gradlew testDebugUnitTest          # run all unit tests (JVM, no emulator)
./gradlew test --tests "*BudgetServiceTest"          # run one test class
./gradlew test --tests "*BudgetServiceTest.testName" # run one test method
./gradlew connectedAndroidTest       # instrumented tests (needs device/emulator) — currently just the default sample test
```

Unit tests run on JUnit 5 (`useJUnitPlatform()` is configured in `app/build.gradle`), with MockK for
mocking. Tests live in `app/src/test/java/com/mas/mobile`, mirroring the main package structure. See
`TestingTools.kt` for shared test doubles (`DummyTaskService`, `DummyEventLogger`) used to avoid mocking
coroutine/analytics plumbing in every test.

`local.properties` (git-ignored) must define `sdk.dir`, `GPT_API_KEY`, and `FREE_CURRENCY_API_KEY` — these
are injected into `BuildConfig` by `app/build.gradle`. A missing `local.properties` or missing keys will
fail the build.

## Architecture

The code is organized by layer, not by feature, under `app/src/main/java/com/mas/mobile`:

- **`domain/`** — business logic and interfaces, split into sub-packages `budget`, `message`, `settings`,
  `analytics`. Each aggregate typically has a plain model class, a `*Repository` interface (domain-facing,
  no Room types), and a `*Service` that holds the actual use-case logic and is what the rest of the app
  depends on. Domain services never talk to Room directly — only to repository interfaces.
- **`repository/`** — implementations of the domain repository interfaces, plus the persistence layer:
  - `repository/db/` — Room setup: `AppDatabase` (entities, migrations, DAOs), `dao/` (Room DAOs), `entity/`
    (Room entities — distinct from the domain models in `domain/`), `config/` (type converters, seed DML).
  - `repository/<area>/*RepositoryImpl.kt` implements a `domain/<area>` repository interface using the DAOs,
    with a `*Mapper` converting between Room entities and domain models.
  - Room migrations are additive and numbered (`MIGRATION_1_2` … in `AppDatabase.kt`); when changing an
    entity, bump `@Database(version = …)` and add a new `MIGRATION_n_(n+1)`, don't edit past migrations.
- **`presentation/`** — UI, MVVM-style:
  - `activity/` — `MainActivity` hosts a single-Activity/Navigation-Component setup (`res/navigation`);
    `activity/fragment/` holds one Fragment per screen, with data binding (`buildFeatures.dataBinding true`).
    `CommonFragment` is the shared base fragment (DI injection point, confirmation/info dialogs, currency
    picker, bottom-nav visibility).
  - `viewmodel/` — one `*ViewModel` per screen/fragment, built via a small custom `ViewModelFactory` that
    wraps `AbstractSavedStateViewModelFactory` (each ViewModel has a nested `Factory` injected by Dagger and
    resolved through `AppComponent`).
  - `adapter/` — `RecyclerView` adapters for list screens.
- **`service/`** — cross-cutting Android services that aren't part of the domain: `SmsListener`
  (BroadcastReceiver for incoming SMS), `NotificationListener` (NotificationListenerService reading other
  apps' notifications), `ScheduledSpendingWorker`/`AppUpdateCheckWorker` (WorkManager jobs), `TaskService`/
  `CoroutineService` (background/blocking task abstraction, mocked as `DummyTaskService` in tests),
  `NotificationService`, `ErrorHandler`, `PermissionService`. `service/ai/` holds the GPT integration
  (`GptChatApiClient`, `GptChatConnector`, `GPTMessageAnalyzer`) used to auto-generate a message
  `Pattern`/`MessageTemplate` from an example SMS/notification.
- **`util/`** — small stateless helpers (`DateTool`, `CurrencyTools`, `FirebaseEventLogger`).

### Dependency injection

Dagger 2, wired entirely in `MasApplication.kt`: one `AppComponent` (`@Singleton`) built from one
`AppModule` providing all repositories, services, and the `AppDatabase` instance. There are no feature-level
Dagger modules/subcomponents. Android framework entry points that can't take constructor injection
(`BroadcastReceiver`, `NotificationListenerService`, `Activity`, `Worker`, `CommonFragment`) use field
injection via `context.appComponent.injectX(this)`, called at the top of their lifecycle entry point
(`onReceive`, `onNotificationPosted`, `onAttach`, etc.). ViewModels are constructor-injected and exposed as
`AppComponent` factory methods (e.g. `fun budgetViewModel(): BudgetViewModel.Factory`).

### Message matching pipeline

This is the core, non-obvious piece of business logic (`domain/message/`):

1. `SmsListener` / `NotificationListener` receive raw `(sender, text, date)` and hand off to
   `MessageService.handleRawMessage`.
2. `MessageService` checks the text against every enabled `MessageTemplate` (`sender` + `Pattern`). A
   `Pattern` is a string with `{amount}` / `{merchant}` placeholders (see `Pattern.kt`) compiled into a
   regex; matching yields `Message.Matched(amount, merchant)`.
3. If nothing matches but a `QualifierService`-based heuristic (keyword qualifiers) thinks the message looks
   financial, the message becomes `Message.Recommended` instead of being dropped, provided
   `Settings.autodetect` is on. Anything else is `Message.Rejected` and not persisted.
4. A user can promote a `Recommended` message: `MessageService.promoteRecommendedMessage` calls
   `MessageAnalyzer` (implemented by `GPTMessageAnalyzer`, backed by the GPT API) to infer a `Pattern` from
   the example text, turns it into a new `MessageTemplate`, and retroactively re-evaluates sibling
   `Recommended` messages from the same sender against the new template.
5. On a `Matched` message, `MessageService` looks up a `Category` by merchant (`CategoryService`) and calls
   `BudgetService.spend(...)` to record an `Expenditure`/`Spending` against the active `Budget`, applying
   currency exchange via `ExchangeRepository` (`FreeCurrencyAPIRepositoryImpl`) when the message currency
   differs from the budget currency.

### Budgets

`BudgetService` (domain/budget) owns the "active budget" concept: budgets are periodic (`Period`: week,
two-weeks, month, quarter, year, configured in `Settings`), auto-created on demand (`getActiveBudget` /
`createNext`) with expenditures pre-populated from active `Category` records. A special fixed-id
"scheduled budget" (`Budget.SCHEDULED_BUDGET_ID`) holds future-dated scheduled spendings, materialized into
real spendings by `ScheduledSpendingWorker` / `BudgetService.createScheduledSpendings()`.
