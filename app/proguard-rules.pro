# Glance widget receivers must not be obfuscated
-keep class dk.codella.vantadot.calendar.widget.CalendarWidgetReceiver { *; }

# WorkManager workers must not be obfuscated
-keep class dk.codella.vantadot.calendar.worker.CalendarUpdateWorker { *; }
-keep class dk.codella.vantadot.calendar.worker.CalendarContentChangeWorker { *; }

# Application class
-keep class dk.codella.vantadot.VantaDotApp { *; }

# Widget configuration activity
-keep class dk.codella.vantadot.calendar.widget.CalendarSettingsActivity { *; }

# Glance ActionCallback must not be obfuscated
-keep class dk.codella.vantadot.calendar.widget.RefreshActionCallback { *; }

# Timer widget
-keep class dk.codella.vantadot.timer.widget.TimerWidgetReceiver { *; }
-keep class dk.codella.vantadot.timer.widget.callbacks.** { *; }
-keep class dk.codella.vantadot.timer.service.TimerService { *; }

# Keep Glance internals
-keep class androidx.glance.** { *; }
