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

- **`calendar/data/`** — `CalendarRepository` queries Android's `CalendarContract.Instances` ContentProvider for today's upcoming events. `CalendarEvent` is a plain data class with computed properties for detecting video conference links and file attachments in event descriptions/locations.
- **`calendar/widget/`** — Glance widget implementation. `CalendarWidget` uses `SizeMode.Exact` with two layout modes (EXPANDED 4×2, FULL 4×4). `CalendarWidgetContent` renders a scrollable event list in both modes (up to 20 events); FULL additionally shows event locations. Includes an urgency system that color-codes events based on time until start (IN_PROGRESS, HIGH, MEDIUM, LOW, SUBTLE, NONE). `RefreshActionCallback` handles manual refresh with animated loading dots. `CalendarWidgetReceiver.onUpdate` pre-loads events so the widget populates immediately on placement. URLs in event locations are cleaned up: mixed text+URL shows only the text, URL-only locations display just the domain name.
- **`calendar/worker/`** — `CalendarUpdateWorker` (WorkManager `CoroutineWorker`) refreshes all widget instances every 15 minutes as a backup. Scheduled in `VantaDotApp.onCreate()` with battery-not-low constraint. `CalendarContentChangeWorker` reactively refreshes when calendar data changes via ContentProvider URI triggers. The primary refresh mechanisms are reactive (content changes) and per-minute (urgency re-render); the periodic worker is a fallback.
- **`timer/data/`** — `TimerState` data class with `displayTime()` and `progress()`. Has three statuses: IDLE, RUNNING, PAUSED. Uses anchor-based timing (`startedAtMs` + `pausedRemainingMs`) for drift-free calculation.
- **`timer/widget/`** — Glance widget implementation. `TimerWidget` uses `SizeMode.Exact` with COMPACT (3×2: time + controls) and FULL (3×3+: time + progress bar + controls). Three action callbacks handle start/pause, reset, and preset cycling. When IDLE, tapping the time display (`‹ 05:00 ›` with chevron affordance) cycles through configured presets. `TimerWidgetContent` shows accent-colored time display when running. Each widget instance runs independently with its own countdown, alarm, and settings. `TimerSettingsActivity` provides per-widget configuration (custom presets, completion sound/vibration, accent color, font size) and is declared as `android:configure` in `timer_widget_info.xml` with `reconfigurable`.
- **`timer/widget/`** — `SecondTickHandler` (in `TimerWidgetReceiver.kt`) uses a coroutine with 1-second delay loop to update all widget instances every second while a timer is running.
- **`timer/service/`** — `TimerAlarmReceiver` fires completion notifications using configurable sound/vibration channels (`timer_complete`, `timer_complete_sound`, `timer_complete_vibrate`, `timer_complete_quiet`). Reads settings from Glance widget state to pick the appropriate channel.
- **`common/`** — `VantaDotWidgetTheme` holds widget-specific color/spacing constants including urgency highlight, accent, and timer urgency colors. `GlanceText` renders text as bitmaps using the custom Doto font (since Glance doesn't support custom fonts natively), and also provides filled/hollow circle rendering, progress bar rendering for the timer widget.
- **`ui/`** — Jetpack Compose UI for `MainActivity`: theme (Material3 dark), `WidgetCatalogScreen` (widget preview cards), `WidgetPreviewCard` (permission request + pin widget flow), `SettingsScreen` (calendar widget configuration: display toggles, text wrapping, max events, accent color, font size, calendar selection; debug-only stub data toggle), and `TimerSettingsScreen` (timer widget configuration: 4 preset slots 1–60 min, completion vibration/sound toggles, accent color, font size).

### Two theming systems

The app uses **two separate theming systems** for different contexts:
1. **`ui/theme/VantaDotTheme`** — Standard Jetpack Compose Material3 theme for the companion activity
2. **`common/VantaDotWidgetTheme`** — Plain object with color/dimension constants for Glance widgets (Glance doesn't support Material3 themes directly)

### Glance-specific patterns

- **`ColorProvider(day, night)`** — Glance's `ColorProvider` requires both day and night colors. Since this is a dark-only widget, pass the same color for both.
- **Custom font rendering** — `GlanceText.renderDotoText()` renders the Doto font to a `Bitmap`, then displays it via `Image(ImageProvider(bitmap))`. This workaround is necessary because Glance `Text` only supports system fonts. Supports `maxWidthDp` for word-wrapping and `maxLines` for ellipsis truncation (used by the "Full event names" setting: off = titles capped at 2 lines, locations at 1 line; on = unlimited wrapping).
- **Widget sizes** — Defined in `CalendarWidgetSizes`: EXPANDED (250×110dp), FULL (250×250dp). Minimum widget width is 250dp (enforced in `calendar_widget_info.xml`). The FULL breakpoint controls whether event locations are shown (requires sufficient height).
- **Minute-tick updates** — `CalendarWidgetReceiver` registers a `MinuteTickReceiver` (via `ACTION_TIME_TICK`, `ACTION_DATE_CHANGED`, `ACTION_TIMEZONE_CHANGED`) so the urgency highlighting updates every minute while the widget is active, and events refresh automatically on date/timezone changes and midnight rollover.

### ProGuard

`proguard-rules.pro` keeps `CalendarWidgetReceiver`, `CalendarSettingsActivity`, `CalendarUpdateWorker`, `VantaDotApp`, `RefreshActionCallback`, `TimerWidgetReceiver`, `TimerSettingsActivity`, timer callbacks, `TimerAlarmReceiver`, and all `androidx.glance.**` classes. Any new widget receiver, worker, action callback, service, settings activity, or Application subclass must be added here.

### Release signing

Release builds are signed via environment variables: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Default keystore path is `~/.android/keystores/vantadot-release.keystore` with alias `vantadot`.

## Key Versions

Managed in `gradle/libs.versions.toml`: AGP 9.1.0, Kotlin 2.2.10, Compose BOM 2024.12.01, Glance 1.1.1, WorkManager 2.10.0. compileSdk/targetSdk = 36, minSdk = 33.
