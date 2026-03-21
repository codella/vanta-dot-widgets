# Glance widget receivers must not be obfuscated
-keep class dk.codella.nothingwidgets.calendar.widget.CalendarWidgetReceiver { *; }

# WorkManager workers must not be obfuscated
-keep class dk.codella.nothingwidgets.calendar.worker.CalendarUpdateWorker { *; }

# Application class
-keep class dk.codella.nothingwidgets.NothingWidgetsApp { *; }

# Keep Glance internals
-keep class androidx.glance.** { *; }
