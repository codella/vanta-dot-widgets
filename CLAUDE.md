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
- **`calendar/widget/`** — Glance widget implementation. `CalendarWidget` uses `SizeMode.Exact` with two layout modes (EXPANDED 4×2, FULL 4×4). `CalendarWidgetContent` renders a scrollable event list in both modes (up to 20 events); FULL additionally shows event locations. Includes an urgency system that color-codes events based on time until start (IN_PROGRESS, HIGH, MEDIUM, LOW, SUBTLE, NONE). `RefreshActionCallback` handles manual refresh with animated loading dots. `CalendarWidgetReceiver.onUpdate` pre-loads events so the widget populates immediately on placement. URLs in event locations are cleaned up: mixed text+URL shows only the text, URL-only locations display just the domain name. When no events remain, shows "No upcoming events" and optionally a daily rotating inspirational quote (controlled by `showEmptyQuote` setting, off by default).
- **`calendar/worker/`** — `CalendarUpdateWorker` (WorkManager `CoroutineWorker`) refreshes all widget instances every 15 minutes as a backup. Scheduled in `VantaDotApp.onCreate()` with battery-not-low constraint. `CalendarContentChangeWorker` reactively refreshes when calendar data changes via ContentProvider URI triggers. The primary refresh mechanisms are reactive (content changes) and per-minute (urgency re-render); the periodic worker is a fallback.
- **`timer/data/`** — `TimerState` data class with `displayTime()` and `progress()`. Has three statuses: IDLE, RUNNING, PAUSED. Uses anchor-based timing (`startedAtMs` + `pausedRemainingMs`) for drift-free calculation.
- **`timer/widget/`** — Glance widget implementation. `TimerWidget` uses `SizeMode.Exact` displaying time + controls. When IDLE, shows preset name above the time with Canvas-rendered chevrons (`<` / `>`) that independently cycle backward/forward through named presets. `TimerWidgetContent` shows accent-colored time display when running. Each widget instance runs independently with its own countdown, alarm, and settings. `TimerSettingsActivity` provides per-widget configuration (2–5 named presets with mm:ss duration, completion sound/vibration, accent color, font size) and is declared as `android:configure` in `timer_widget_info.xml` with `reconfigurable`. Presets stored as JSON array in a single preference key.
- **`timer/widget/`** — `SecondTickHandler` (in `TimerWidgetReceiver.kt`) uses a coroutine with 1-second delay loop to update all widget instances every second while a timer is running.
- **`timer/service/`** — `TimerAlarmReceiver` fires completion notifications using configurable sound/vibration channels (`timer_complete`, `timer_complete_sound`, `timer_complete_vibrate`, `timer_complete_quiet`). Reads settings from Glance widget state to pick the appropriate channel.
- **`metronome/data/`** — `MetronomeWidgetState` data class with two statuses: IDLE, PLAYING. Tracks BPM, current beat position (0-indexed), and beats per bar. `MetronomeSoundChoice` enum provides three sound packs (Click, Wood Block, Digital Beep) with normal and accent resource IDs.
- **`metronome/widget/`** — Glance widget implementation. `MetronomeWidget` uses `SizeMode.Exact` at 3×2 (180×110dp). When IDLE, shows preset name with cycling chevrons, large BPM display, ±1 fine-adjust buttons, PLAY button, and hollow beat dots. When PLAYING, shows accent-colored BPM with filled/hollow beat dots animating the current beat. `MetronomeSettingsActivity` provides per-widget configuration (2–5 named BPM presets 30–300, time signature, sound choice, accent first beat, vibration, accent color, font size). Each widget instance runs independently.
- **`metronome/service/`** — `MetronomeService` is a foreground service (`mediaPlayback` type) that handles precise audio playback using `SoundPool` + `Handler.postDelayed()` beat loop. Plays normal or accent clicks, optionally vibrates, and updates the widget's `currentBeat` preference on each tick. Shows a persistent "Metronome — N BPM" notification while running.
- **`banner/data/`** — `BannerVibe` enum (Scroll, Bounce, Typewriter) for animation styles. `BannerMessage` is a plain data class.
- **`banner/widget/`** — Glance widget implementation. `BannerWidget` uses `SizeMode.Exact` at 2×1 (120×55dp minimum, horizontally resizable). Renders scrolling marquee text using `GlanceText.renderMarqueeFrame()` which draws the Doto font into a viewport-sized bitmap at a configurable scroll offset. Three animation vibes: Scroll (continuous left marquee), Bounce (ping-pong), Typewriter (character-by-character reveal). `BannerScrollTickHandler` runs a coroutine loop at 30–80ms intervals to advance scroll offset and update all widget instances. `BannerScreenReceiver` pauses/resumes the tick handler on screen off/on. Tap toggles pause/resume via `TapActionCallback`. Multiple messages rotate automatically after each completes its animation cycle. `BannerSettingsActivity` provides per-widget configuration (1–10 custom messages, vibe, scroll speed, gap between messages 1–10s, accent color, font size). Each widget instance runs independently.
- **`binaryclock/data/`** — `BinaryClockDotShape` enum (Circle, Square) for the dot rendering style.
- **`binaryclock/widget/`** — Glance widget implementation. `BinaryClockWidget` uses `SizeMode.Exact` at 3×2 (180×110dp). Displays current time as a BCD (Binary-Coded Decimal) dot grid: 4 rows (bit values 8, 4, 2, 1) × 4–6 columns (H tens, H ones, M tens, M ones, optionally S tens, S ones). `GlanceText.renderBinaryClockFace()` renders the entire grid as a single bitmap with optional bit labels (8/4/2/1) and column labels (H/M/S). "On" bits use accent color, "off" bits use dim grey with hollow style. Optional digital time readout below the grid. `BinaryClockWidgetReceiver` registers a `ClockMinuteTickReceiver` (via `ACTION_TIME_TICK`, `ACTION_TIMEZONE_CHANGED`) for minute-level updates. `BinaryClockSecondTickHandler` provides 1-second ticking when any widget has "show seconds" enabled; started/stopped dynamically from `onUpdate` and settings saves. `BinaryClockSettingsActivity` provides per-widget configuration (show seconds, 24h format, show digital time, show bit/column labels, dot shape, accent color, font size). Each widget instance runs independently.
- **`common/`** — `VantaDotWidgetTheme` holds widget-specific color/spacing constants including urgency highlight, accent, and timer urgency colors. `GlanceText` renders text as bitmaps using the custom Doto font (since Glance doesn't support custom fonts natively), and also provides filled/hollow circle rendering, progress bar rendering for the timer widget, binary clock face rendering, and marquee frame rendering for the banner widget.
- **`ui/`** — Jetpack Compose UI for `MainActivity`: theme (Material3 dark), `WidgetCatalogScreen` (widget preview cards), `WidgetPreviewCard` (permission request + pin widget flow), `SettingsScreen` (calendar widget configuration: display toggles, text wrapping, empty calendar quote, max events, accent color, font size, calendar selection; debug-only stub data toggle), `TimerSettingsScreen` (timer widget configuration: 4 preset slots 1–60 min, completion vibration/sound toggles, accent color, font size), `MetronomeSettingsScreen` (metronome configuration: BPM presets, time signature, sound choice, feedback toggles, accent color, font size), `BinaryClockSettingsScreen` (binary clock configuration: show seconds, 24h format, digital time readout, bit/column labels, dot shape, accent color, font size), and `BannerSettingsScreen` (banner configuration: 1–10 custom messages, vibe/animation style, scroll speed, accent color, font size).

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

`proguard-rules.pro` keeps `CalendarWidgetReceiver`, `CalendarSettingsActivity`, `CalendarUpdateWorker`, `VantaDotApp`, `RefreshActionCallback`, `TimerWidgetReceiver`, `TimerSettingsActivity`, timer callbacks, `TimerAlarmReceiver`, `MetronomeWidgetReceiver`, `MetronomeSettingsActivity`, metronome callbacks, `MetronomeService`, `BinaryClockWidgetReceiver`, `BinaryClockSettingsActivity`, `BannerWidgetReceiver`, `BannerSettingsActivity`, `TapActionCallback`, and all `androidx.glance.**` classes. Any new widget receiver, worker, action callback, service, settings activity, or Application subclass must be added here.

### Release signing

Release builds are signed via environment variables: `KEYSTORE_PATH`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`. Default keystore path is `~/.android/keystores/vantadot-release.keystore` with alias `vantadot`.

## Key Versions

Managed in `gradle/libs.versions.toml`: AGP 9.1.0, Kotlin 2.2.10, Compose BOM 2024.12.01, Glance 1.1.1, WorkManager 2.10.0. compileSdk/targetSdk = 36, minSdk = 33.
