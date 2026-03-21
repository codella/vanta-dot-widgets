# Nothing Widgets

Minimalist home screen widgets for Android, inspired by the Nothing OS aesthetic. Dark, clean, dot-matrix style — built with Jetpack Glance.

## Calendar Widget

Shows upcoming events from your device calendar in a resizable widget with three layout modes:

- **Compact (2x2)** — date header + 2 events
- **Expanded (4x2)** — date header + 4 events with color dots and times
- **Full (4x4)** — date header + scrollable list of up to 8 events with locations

The widget uses the [Doto](https://fonts.google.com/specimen/Doto) dot-matrix font for headers and auto-refreshes every 15 minutes via WorkManager.

## Requirements

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

After installing, long-press on your home screen, tap "Widgets", and find **Nothing Widgets > Calendar**. The app will ask for calendar read permission on first launch.

## Project Structure

```
app/src/main/java/dk/codella/nothingwidgets/
├── calendar/
│   ├── data/          CalendarEvent model + CalendarRepository (ContentProvider queries)
│   ├── widget/        Glance widget: CalendarWidget, CalendarWidgetContent, sizes
│   └── worker/        CalendarUpdateWorker (periodic refresh)
├── common/            Widget theme constants + Doto font bitmap renderer
└── ui/                Companion app: Material3 theme, widget catalog screen
```

## Tests

```bash
./gradlew testDebugUnitTest
```

## License

TBD
