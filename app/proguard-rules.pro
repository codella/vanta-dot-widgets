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

# Timer widget configuration activity
-keep class dk.codella.vantadot.timer.widget.TimerSettingsActivity { *; }

# Timer widget receiver and callbacks
-keep class dk.codella.vantadot.timer.widget.TimerWidgetReceiver { *; }
-keep class dk.codella.vantadot.timer.widget.StartPauseActionCallback { *; }
-keep class dk.codella.vantadot.timer.widget.ResetActionCallback { *; }
-keep class dk.codella.vantadot.timer.widget.PresetActionCallback { *; }

# Timer alarm receiver
-keep class dk.codella.vantadot.timer.service.TimerAlarmReceiver { *; }

# Keep Glance internals
-keep class androidx.glance.** { *; }
