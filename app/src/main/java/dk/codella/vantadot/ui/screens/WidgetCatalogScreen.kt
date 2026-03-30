package dk.codella.vantadot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.codella.vantadot.banner.widget.BannerWidgetReceiver
import dk.codella.vantadot.calendar.widget.CalendarWidgetReceiver
import dk.codella.vantadot.binaryclock.widget.BinaryClockWidgetReceiver
import dk.codella.vantadot.metronome.widget.MetronomeWidgetReceiver
import dk.codella.vantadot.timer.widget.TimerWidgetReceiver
import dk.codella.vantadot.ui.theme.VantaDotBlack

@Composable
fun WidgetCatalogScreen(
    hasCalendarPermission: Boolean,
    onRequestCalendarPermission: () -> Unit,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaDotBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = "VANTA DOT WIDGETS",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                WidgetPreviewCard(
                    widgetName = "CALENDAR",
                    widgetDescription = "Upcoming events from your calendar",
                    hasPermission = hasCalendarPermission,
                    onRequestPermission = onRequestCalendarPermission,
                    receiverClass = CalendarWidgetReceiver::class.java,
                )
            }
            item {
                WidgetPreviewCard(
                    widgetName = "TIMER",
                    widgetDescription = "Countdown timer with preset durations",
                    hasPermission = hasNotificationPermission,
                    onRequestPermission = onRequestNotificationPermission,
                    receiverClass = TimerWidgetReceiver::class.java,
                )
            }
            item {
                WidgetPreviewCard(
                    widgetName = "METRONOME",
                    widgetDescription = "Practice metronome with BPM presets",
                    hasPermission = hasNotificationPermission,
                    onRequestPermission = onRequestNotificationPermission,
                    receiverClass = MetronomeWidgetReceiver::class.java,
                )
            }
            item {
                WidgetPreviewCard(
                    widgetName = "BINARY CLOCK",
                    widgetDescription = "BCD binary clock with dot grid display",
                    hasPermission = true,
                    onRequestPermission = {},
                    receiverClass = BinaryClockWidgetReceiver::class.java,
                )
            }
            item {
                WidgetPreviewCard(
                    widgetName = "BANNER",
                    widgetDescription = "Scrolling dot-matrix marquee banner",
                    hasPermission = true,
                    onRequestPermission = {},
                    receiverClass = BannerWidgetReceiver::class.java,
                )
            }
        }
    }
}
