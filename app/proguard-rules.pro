# Glance widget receivers must not be obfuscated
-keep class dk.codella.vantadot.calendar.widget.CalendarWidgetReceiver { *; }

# WorkManager workers must not be obfuscated
-keep class dk.codella.vantadot.calendar.worker.CalendarUpdateWorker { *; }

# Application class
-keep class dk.codella.vantadot.VantaDotApp { *; }

# Glance ActionCallback must not be obfuscated
-keep class dk.codella.vantadot.calendar.widget.RefreshActionCallback { *; }

# Keep Glance internals
-keep class androidx.glance.** { *; }
