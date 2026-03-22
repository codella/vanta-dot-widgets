# Glance widget receivers must not be obfuscated
-keep class dk.codella.phosphor.calendar.widget.CalendarWidgetReceiver { *; }

# WorkManager workers must not be obfuscated
-keep class dk.codella.phosphor.calendar.worker.CalendarUpdateWorker { *; }

# Application class
-keep class dk.codella.phosphor.PhosphorApp { *; }

# Glance ActionCallback must not be obfuscated
-keep class dk.codella.phosphor.calendar.widget.RefreshActionCallback { *; }

# Keep Glance internals
-keep class androidx.glance.** { *; }
