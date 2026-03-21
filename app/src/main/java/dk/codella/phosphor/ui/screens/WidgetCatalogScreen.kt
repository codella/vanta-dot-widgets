package dk.codella.phosphor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.codella.phosphor.calendar.widget.CalendarWidgetReceiver
import dk.codella.phosphor.ui.theme.PhosphorBlack

@Composable
fun WidgetCatalogScreen(
    hasCalendarPermission: Boolean,
    onRequestPermission: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PhosphorBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Text(
            text = "PHOSPHOR WIDGETS",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
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
                    onRequestPermission = onRequestPermission,
                    receiverClass = CalendarWidgetReceiver::class.java,
                )
            }
        }
    }
}
