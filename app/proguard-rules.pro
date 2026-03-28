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
-keep class dk.codella.vantadot.timer.widget.CyclePresetActionCallback { *; }

# Timer alarm receiver
-keep class dk.codella.vantadot.timer.service.TimerAlarmReceiver { *; }

# Metronome widget
-keep class dk.codella.vantadot.metronome.widget.MetronomeWidgetReceiver { *; }
-keep class dk.codella.vantadot.metronome.widget.MetronomeSettingsActivity { *; }
-keep class dk.codella.vantadot.metronome.widget.PlayStopActionCallback { *; }
-keep class dk.codella.vantadot.metronome.widget.AdjustBpmActionCallback { *; }
-keep class dk.codella.vantadot.metronome.widget.CycleMetronomePresetActionCallback { *; }
-keep class dk.codella.vantadot.metronome.service.MetronomeService { *; }

# Keep Glance internals
-keep class androidx.glance.** { *; }
