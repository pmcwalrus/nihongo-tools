# Architecture Guide

## Purpose

`Nihongo Tools` is a desktop application on Kotlin for small Japanese-learning utilities.

The project currently contains:

1. A kanji counter for documents with CSV export.
2. An audio downloader for Marugoto dictionary fragments.

The application is designed so new utilities can be added as independent screens without rewriting the app shell.

## High-Level Structure

The application has a simple layered structure:

1. Entry point and app shell.
2. Tool registry and navigation.
3. Tool-specific UI.
4. Tool-specific business services.
5. Shared utility helpers.

Main idea:

- the app shows a list of tools
- the user opens one tool
- the tool screen collects input
- long-running work is executed in coroutines on `Dispatchers.IO`
- progress is pushed back into Compose state
- the user can return to the main menu

## Directory Layout

Top-level project files:

- [build.gradle.kts](/Users/varenikab/nihongo_tools/build.gradle.kts): Gradle build, dependencies, Compose setup, native packaging.
- [settings.gradle.kts](/Users/varenikab/nihongo_tools/settings.gradle.kts): Gradle project name.
- [gradle.properties](/Users/varenikab/nihongo_tools/gradle.properties): Gradle/JVM settings.
- [README.md](/Users/varenikab/nihongo_tools/README.md): user-facing description.
- [architecture.md](/Users/varenikab/nihongo_tools/architecture.md): this architecture guide.

Source tree:

- [Main.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/Main.kt): application entry point.
- [model/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/model): tool contracts.
- [tools/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools): tool registry and tool implementations.
- [service/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/service): shared domain services.
- [ui/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/ui): shared Compose UI scaffolding/components.
- [util/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/util): shared helpers.
- [src/test/kotlin](/Users/varenikab/nihongo_tools/src/test/kotlin): tests.

## Runtime Flow

### App startup

Startup begins in [Main.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/Main.kt).

- `main()` starts a Compose desktop application.
- A single top-level window is created.
- `NihongoToolsApp()` renders the app UI.

### App shell and navigation

Navigation is intentionally minimal and local.

Relevant files:

- [NihongoToolsApp.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/ui/NihongoToolsApp.kt)
- [ToolCatalog.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/ToolCatalog.kt)
- [AppTool.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/model/AppTool.kt)

Flow:

1. `ToolCatalog.tools` returns a list of available tools.
2. `AppRoot()` stores the currently selected tool in local Compose state.
3. If no tool is selected, `ToolMenu()` displays the list.
4. If a tool is selected, its `Content(onBack)` composable is rendered.
5. `onBack` clears the selected tool and returns to the menu.

This means the current navigation model is:

- single-window
- single-level menu
- one active tool screen at a time
- no deep routing system

That simplicity is intentional and should be preserved unless requirements change significantly.

## Core Contract: `AppTool`

The extension point for new features is [AppTool.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/model/AppTool.kt).

Each tool must provide:

- `id`: stable internal identifier
- `title`: user-facing title
- `description`: short menu description
- `Content(onBack)`: full Compose UI for that tool

Implications:

- every tool owns its own screen
- tool UI is isolated from other tools
- new tools do not need changes in navigation logic beyond registration

## Tool Registry

[ToolCatalog.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/ToolCatalog.kt) is the central registry.

Current registry:

- `KanjiCounterTool`
- `MarugotoAudioTool`

To add a new tool, the minimal integration path is:

1. Create a new tool class implementing `AppTool`.
2. Add service classes if business logic is non-trivial.
3. Register the tool instance in `ToolCatalog.tools`.

## Shared UI Layer

Shared UI helpers live in [ui/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/ui).

### `NihongoToolsApp.kt`

Responsibilities:

- theme setup
- window-wide root content
- main menu rendering
- simple tool selection navigation
- shared `ToolScaffold` for tool pages

Important pieces:

- `NihongoToolsApp()`: top-level Material theme
- `AppRoot()`: top-level selected-tool state
- `ToolMenu()`: list of tools with scrollbar
- `ToolScaffold()`: shared header with back button

### `UiPieces.kt`

Shared reusable UI building blocks:

- `FilePickerRow(...)`: generic file chooser row
- `ProgressSection(...)`: linear progress bar with status text
- `ScrollableContent(...)`: vertically scrollable container with visible scrollbar

Guideline:

- keep generic widgets here
- keep tool-specific widgets in tool files unless they become shared by at least two tools

## Shared Utility Layer

Shared helpers live in [util/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/util).

### `FileDialogs.kt`

Responsibilities:

- open file chooser
- open directory chooser
- fall back between Swing and AWT dialog behavior
- keep file/directory selection logic out of business services

This is a UI-adjacent helper and should stay free of domain logic.

### `CsvWriter.kt`

Responsibility:

- write UTF-8 CSV with escaping

This is currently used by the kanji counter service.

### `FileNameSanitizer.kt`

Responsibility:

- sanitize output filenames for cross-platform safety

Currently used by audio downloading to build valid `.mp3` names.

## Shared Service Layer

Shared domain services live in [service/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/service).

### `DocumentTextExtractor.kt`

Responsibility:

- extract text from user-selected documents through Apache Tika

Why this matters:

- it centralizes document parsing in one place
- tool logic does not need to know specific file formats
- adding extraction tweaks should happen here first, not in individual tools

## Tool 1: Kanji Counter

Relevant files:

- [KanjiCounterTool.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/kanji/KanjiCounterTool.kt)
- [KanjiCounterService.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/kanji/KanjiCounterService.kt)
- [KanjiCounterServiceTest.kt](/Users/varenikab/nihongo_tools/src/test/kotlin/nihongo/tools/tools/kanji/KanjiCounterServiceTest.kt)

### UI responsibilities

`KanjiCounterTool` owns:

- input file selection
- optional exclusion file selection
- optional result subfolder name input
- triggering destination folder selection when the run button is pressed
- progress display
- success/error summary display

State currently stored in Compose local state:

- `sourceFile`
- `exclusionFile`
- `resultFolderName`
- `progress`
- `status`
- `resultSummary`
- `isRunning`

### Service responsibilities

`KanjiCounterService` owns:

- extracting source text
- extracting exclusion text
- converting exclusion text into a set of kanji
- counting kanji occurrences in source text
- sorting by descending count
- writing CSV output
- reporting progress stages

### Counting rule

Current kanji detection rule:

- `Character.UnicodeScript.of(char.code) == Character.UnicodeScript.HAN`

This includes Han-script characters. If future requirements need stricter Japanese-only filtering, this logic should be adjusted in [KanjiCounterService.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/kanji/KanjiCounterService.kt).

### Output behavior

Output file naming rule:

- `${sourceFile.nameWithoutExtension}_kanji_counts.csv`

Output directory rule:

- if the user enters a subfolder name, the tool creates that subfolder under the selected base folder
- otherwise it writes directly into the selected base folder

## Tool 2: Marugoto Audio Downloader

Relevant files:

- [MarugotoAudioTool.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/marugoto/MarugotoAudioTool.kt)
- [MarugotoAudioService.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/marugoto/MarugotoAudioService.kt)
- [MarugotoAudioServiceTest.kt](/Users/varenikab/nihongo_tools/src/test/kotlin/nihongo/tools/tools/marugoto/MarugotoAudioServiceTest.kt)

### UI responsibilities

`MarugotoAudioTool` owns:

- pasted source text input
- optional result subfolder name input
- destination folder selection at action time
- progress display
- success/error summary display

State currently stored in Compose local state:

- `rawText`
- `resultFolderName`
- `progress`
- `status`
- `resultSummary`
- `isRunning`

### Service responsibilities

`MarugotoAudioService` owns:

- parsing text fragments
- extracting Japanese labels from `_jpn`
- extracting `.mp3` paths from `data-audio`
- resolving relative paths against `https://www.marugoto-online.jp/`
- downloading files
- generating unique output file names
- reporting progress

### Parsing model

The parser is intentionally targeted to a known Marugoto fragment format, not to full HTML correctness.

Current strategy:

- regex locates `_jpn` and `data-audio` pairs
- Jsoup extracts cleaned text from the `_jpn` HTML fragment

If the Marugoto source format changes, the first place to update is [MarugotoAudioService.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/marugoto/MarugotoAudioService.kt).

### File naming

The downloaded file name comes from the Japanese label.

Rules:

- forbidden file system characters are replaced
- duplicate names get suffixes like `(2)`, `(3)`, etc.

## Concurrency Model

Long-running work is launched from UI via coroutines.

Pattern used in both tools:

1. Button click validates input.
2. Potential folder selection is performed.
3. UI state switches to running mode.
4. Heavy work is executed inside `withContext(Dispatchers.IO)`.
5. Services call back with progress updates.
6. Progress/state is reflected in Compose state.
7. Success or failure updates the summary/status.

Important detail:

- services currently switch to `Dispatchers.Main` when invoking progress callbacks
- this avoids unsafe direct UI-state mutation from background threads

If introducing more complex async flows, preserve the distinction:

- I/O and parsing on background dispatcher
- Compose state updates on main dispatcher

## Error Handling Strategy

Error handling is intentionally simple and user-visible.

Current pattern:

- UI wraps tool execution with `runCatching`
- errors are converted into readable `status` messages
- success produces a short summary block

This means:

- there is no centralized error bus
- there is no logging framework yet
- exceptions are expected to bubble to the screen-level handler

If logging is added later, keep user-facing messages simple and avoid exposing stack traces directly in the UI.

## Testing Strategy

Current tests are lightweight smoke tests:

- [KanjiCounterServiceTest.kt](/Users/varenikab/nihongo_tools/src/test/kotlin/nihongo/tools/tools/kanji/KanjiCounterServiceTest.kt): validates kanji detection behavior
- [MarugotoAudioServiceTest.kt](/Users/varenikab/nihongo_tools/src/test/kotlin/nihongo/tools/tools/marugoto/MarugotoAudioServiceTest.kt): validates parser output for a representative fragment

There are no UI tests yet.

When changing behavior:

- add or update service-level tests first
- avoid making parsing/counting changes without tests

## Build and Packaging

Build configuration lives in [build.gradle.kts](/Users/varenikab/nihongo_tools/build.gradle.kts).

Key points:

- Kotlin JVM + Compose Multiplatform Desktop
- native distribution targets include `DMG`, `MSI`, `EXE`
- explicit Java modules are listed for packaging
- `java.net.http` is required for the audio downloader

Common commands:

- run app: `./gradlew run`
- tests: `./gradlew test`
- macOS package: `./gradlew packageDmg`
- Windows packages: `gradlew.bat packageMsi`, `gradlew.bat packageExe`

Platform rule:

- build macOS packages on macOS
- build Windows packages on Windows

## How to Safely Add a New Tool

Recommended implementation path:

1. Create a package under [tools/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools).
2. Add a service class for business logic if the behavior is more than trivial UI glue.
3. Add a tool class implementing `AppTool`.
4. Reuse `ToolScaffold`, `ProgressSection`, `ScrollableContent`, `FilePickerRow` where appropriate.
5. Register the tool in [ToolCatalog.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/ToolCatalog.kt).
6. Add service-level tests under [src/test/kotlin](/Users/varenikab/nihongo_tools/src/test/kotlin).
7. Run `./gradlew test`.

Heuristics:

- domain logic belongs in services, not in composables
- file/network/document parsing logic should stay out of UI files
- reusable UI should move into [ui/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/ui)
- reusable non-UI helpers should move into [util/](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/util)

## How to Modify Existing Tools

### If changing UI only

Usually edit:

- the specific tool file in `tools/...`
- possibly [UiPieces.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/ui/UiPieces.kt) if the UI pattern should become reusable

Do not move business logic into composables just because it is faster.

### If changing parsing/counting/downloading behavior

Usually edit:

- the tool service file
- tests for that service

Examples:

- document extraction bug: [DocumentTextExtractor.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/service/DocumentTextExtractor.kt)
- kanji counting logic: [KanjiCounterService.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/kanji/KanjiCounterService.kt)
- Marugoto fragment parsing/downloading: [MarugotoAudioService.kt](/Users/varenikab/nihongo_tools/src/main/kotlin/nihongo/tools/tools/marugoto/MarugotoAudioService.kt)

### If changing packaging or dependencies

Edit:

- [build.gradle.kts](/Users/varenikab/nihongo_tools/build.gradle.kts)

Be careful with:

- Compose plugin compatibility
- native packaging modules
- runtime module availability for packaged apps

## Known Architectural Constraints

These are current intentional simplifications:

1. No dependency injection framework.
2. No multi-module Gradle setup.
3. No persistent settings storage.
4. No view models or external state containers.
5. No plugin loading at runtime.
6. No centralized logging/telemetry.

This is acceptable for the current project size.

If the project grows, likely next structural steps would be:

- introduce persistent app settings
- extract more service interfaces for testing
- add a lightweight state-holder/view-model layer
- split features into clearer packages or modules

## Agent Notes

For an AI agent modifying this project, the safest assumptions are:

1. Preserve the `AppTool` + `ToolCatalog` extension model.
2. Keep Compose UI files focused on state and presentation.
3. Keep parsing, document processing, downloading, and export logic in services/utilities.
4. Update tests when behavior changes.
5. Run `./gradlew test` after non-trivial changes.
6. If packaging-related code changes, consider whether `packageDmg` / `packageMsi` runtime modules are affected.

When uncertain where a change belongs:

- UI interaction and local page state -> tool composable
- shared UI widget -> `ui`
- cross-tool helper -> `util`
- document/network/business logic -> `service` or tool-specific service
- app registration/navigation -> `ToolCatalog` or `NihongoToolsApp`
