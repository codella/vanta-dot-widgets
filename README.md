# Vanta Dot Widgets

Minimalist home screen widgets for Android, inspired by the Nothing OS aesthetic. Dark, clean, dot-matrix style — built with Jetpack Glance.

## Calendar Widget

Shows upcoming events from your device calendar in a resizable widget with three layout modes:

- **Compact (2x2)** — date header + 2 events
- **Expanded (4x2)** — date header + 4 events with color dots and times
- **Full (4x4)** — date header + scrollable list of up to 8 events with times, locations, and icons

### Features

- **Urgency highlighting** — events are color-coded based on time until start, from subtle grey through to red for in-progress events
- **Video conference detection** — shows a camera icon when Zoom, Meet, Teams, WebEx, or other video links are found in event details
- **Attachment detection** — shows a paperclip icon when Google Drive, Dropbox, OneDrive, Notion, or other file links are found
- **All-day event grouping** — all-day events are displayed in a separate section with hollow dot indicators
- **Manual refresh** — tap the header to trigger a refresh with animated loading dots
- **Auto-refresh** — WorkManager updates every 15 minutes; a minute-tick receiver keeps urgency colors current
- **Empty state quotes** — when no events are upcoming, the widget shows one of 47 inspirational quotes (rotated daily)
- **Custom font** — uses the [Doto](https://fonts.google.com/specimen/Doto) dot-matrix font, rendered as bitmaps since Glance doesn't support custom fonts

## Requirements

- Android 13+ (API 33)
- Android SDK (API 36) — install via Android Studio or `sdkmanager "platforms;android-36"`
- JDK 17 — the project includes a [mise](https://mise.jdx.dev/) config (`mise.toml`) that manages this automatically

## Build

```bash
# If using mise for JDK management:
mise install

# Build the debug APK:
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

If you're not using mise, set `JAVA_HOME` to any JDK 17 installation before running Gradle.

## Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

After installing, long-press on your home screen, tap "Widgets", and find **Vanta Dot > Calendar**. The companion app lets you preview widgets and grant calendar read permission.

## Project Structure

```
app/src/main/java/dk/codella/vantadot/
├── VantaDotApp.kt             Application class — schedules WorkManager refresh
├── MainActivity.kt            Companion activity — permission UI & widget catalog
├── calendar/
│   ├── data/
│   │   ├── CalendarEvent.kt       Event model with video/attachment detection
│   │   ├── CalendarRepository.kt  ContentProvider queries (7-day window, max 8 events)
│   │   └── StubCalendarData.kt    Mock data for debug builds
│   ├── widget/
│   │   ├── CalendarWidget.kt          Glance widget with responsive breakpoints
│   │   ├── CalendarWidgetReceiver.kt  BroadcastReceiver + minute-tick updater
│   │   ├── CalendarWidgetContent.kt   All composable UI (urgency, layouts, empty state)
│   │   ├── CalendarWidgetSizes.kt     Size breakpoint constants
│   │   └── RefreshActionCallback.kt   Manual refresh with loading animation
│   └── worker/
│       └── CalendarUpdateWorker.kt    Periodic refresh every 15 minutes
├── common/
│   ├── VantaDotWidgetTheme.kt    Widget colors, urgency palette, dimensions
│   └── GlanceText.kt            Doto font bitmap rendering + circle indicators
└── ui/
    ├── screens/
    │   ├── WidgetCatalogScreen.kt    Main screen with debug stub toggle
    │   └── WidgetPreviewCard.kt      Permission request + pin widget button
    └── theme/
        ├── VantaDotTheme.kt          Material3 dark theme for companion activity
        ├── Color.kt                  Color constants
        └── Type.kt                   Typography with Doto font
```

## Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run a single test class
./gradlew testDebugUnitTest --tests "dk.codella.vantadot.calendar.data.CalendarRepositoryTest"
```

## Tech Stack

| Category | Library | Version |
|----------|---------|---------|
| Build | Android Gradle Plugin | 9.1.0 |
| Language | Kotlin | 2.2.10 |
| UI (activity) | Jetpack Compose + Material3 | BOM 2024.12.01 |
| UI (widget) | Jetpack Glance | 1.1.1 |
| Background work | WorkManager | 2.10.0 |
| Target SDK | Android | 36 |
| Min SDK | Android | 33 (Android 13) |

## License

This project is licensed under the [Apache License 2.0](LICENSE).
