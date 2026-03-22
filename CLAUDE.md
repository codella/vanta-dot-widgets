# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Prerequisites: JDK 17 (managed via mise) + Android SDK at ~/Android/Sdk
export JAVA_HOME="$HOME/.local/share/mise/installs/java/temurin-17.0.18+8"
export PATH="$JAVA_HOME/bin:$PATH"
export ANDROID_HOME="$HOME/Android/Sdk"

# Build debug APK
./gradlew assembleDebug

# Run all unit tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew testDebugUnitTest --tests "dk.codella.vantadot.calendar.data.CalendarRepositoryTest"

# Clean build
./gradlew clean assembleDebug

# Build signed release APK (requires signing env vars)
./gradlew assembleRelease
```

Gradle needs network access to resolve dependencies from `dl.google.com`, `repo.maven.apache.org`, `plugins.gradle.org`, and `services.gradle.org`. If running in a sandboxed environment, these domains must be allowed or the sandbox disabled.

## Architecture

This is a Nothing OS-styled Android home screen widget app built with **Jetpack Glance** (not RemoteViews). The app has a companion activity for previewing widgets and requesting permissions.

### Package: `dk.codella.vantadot`

### Layer structure

- **`calendar/data/`** — `CalendarRepository` queries Android's `CalendarContract.Instances` ContentProvider for 7 days of upcoming events (max 8). `CalendarEvent` is a plain data class with computed properties for detecting video conference links and file attachments in event descriptions/locations.
- **`calendar/widget/`** — Glance widget implementation. `CalendarWidget` uses `SizeMode.Responsive` with three breakpoints (COMPACT 2×2, EXPANDED 4×2, FULL 4×4). `CalendarWidgetContent` renders different layouts based on `LocalSize` — compact shows 2 events, expanded shows 4, full shows 8 in a `LazyColumn`. Includes an urgency system that color-codes events based on time until start (IN_PROGRESS, HIGH, MEDIUM, LOW, SUBTLE, NONE). `RefreshActionCallback` handles manual refresh with animated loading dots.
- **`calendar/worker/`** — `CalendarUpdateWorker` (WorkManager `CoroutineWorker`) refreshes all widget instances every 15 minutes. Scheduled in `VantaDotApp.onCreate()` with battery-not-low constraint.
- **`common/`** — `VantaDotWidgetTheme` holds widget-specific color/spacing constants including urgency highlight and accent colors. `GlanceText` renders text as bitmaps using the custom Doto font (since Glance doesn't support custom fonts natively), and also provides filled/hollow circle rendering for event indicators.
- **`ui/`** — Jetpack Compose UI for `MainActivity`: theme (Material3 dark), `WidgetCatalogScreen` (with debug-only stub data toggle), and `WidgetPreviewCard` (permission request + pin widget flow).

### Vendored `androidx/` directory

`androidx/glance/appwidget/lazy/LazyList.kt` is a vendored copy of the AndroidX Glance `LazyColumn` implementation (Apache 2.0). This is **not a published Glance API** — it provides scrollable list support used in the 4×4 full-size widget layout. This directory is untracked in git.

### Two theming systems

The app uses **two separate theming systems** for different contexts:
1. **`ui/theme/VantaDotTheme`** — Standard Jetpack Compose Material3 theme for the companion activity
2. **`common/VantaDotWidgetTheme`** — Plain object with color/dimension constants for Glance widgets (Glance doesn't support Material3 themes directly)

### Glance-specific patterns

- **`ColorProvider(day, night)`** — Glance's `ColorProvider` requires both day and night colors. Since this is a dark-only widget, pass the same color for both.
- **Custom font rendering** — `GlanceText.renderDotoText()` renders the Doto font to a `Bitmap`, then displays it via `Image(ImageProvider(bitmap))`. This workaround is necessary because Glance `Text` only supports system fonts.
- **Widget sizes** — Defined in `CalendarWidgetSizes`: COMPACT (110×110dp), EXPANDED (250×110dp), FULL (250×250dp). The widget adapts its layout based on which size breakpoint is active.
- **Minute-tick updates** — `CalendarWidgetReceiver` registers a `MinuteTickReceiver` (via `ACTION_TIME_TICK`) so the urgency highlighting updates every minute while the widget is active.

### ProGuard

`proguard-rules.pro` keeps `CalendarWidgetReceiver`, `CalendarUpdateWorker`, `VantaDotApp`, `RefreshActionCallback`, and all `androidx.glance.**` classes. Any new widget receiver, worker, action callback, or Application subclass must be added here.

### Release signing

Release builds are signed via environment variables: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Default keystore path is `~/.android/keystores/vantadot-release.keystore` with alias `vantadot`.

## Key Versions

Managed in `gradle/libs.versions.toml`: AGP 9.1.0, Kotlin 2.2.10, Compose BOM 2024.12.01, Glance 1.1.1, WorkManager 2.10.0. compileSdk/targetSdk = 36, minSdk = 33.
