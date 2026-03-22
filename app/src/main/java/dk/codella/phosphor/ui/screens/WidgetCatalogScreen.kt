package dk.codella.phosphor.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.codella.phosphor.BuildConfig
import dk.codella.phosphor.calendar.widget.CalendarWidget
import dk.codella.phosphor.calendar.widget.CalendarWidgetReceiver
import dk.codella.phosphor.ui.theme.PhosphorBlack
import dk.codella.phosphor.ui.theme.PhosphorRed
import dk.codella.phosphor.ui.theme.PhosphorWhite

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
            if (BuildConfig.DEBUG) {
                item {
                    StubDataToggle()
                }
            }
        }
    }
}

@Composable
private fun StubDataToggle() {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences(CalendarWidget.PREFS_NAME, Context.MODE_PRIVATE)
    var useStub by remember { mutableStateOf(prefs.getBoolean(CalendarWidget.USE_STUB_KEY, false)) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "USE STUB DATA",
            style = MaterialTheme.typography.labelLarge,
            color = PhosphorWhite,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = useStub,
            onCheckedChange = { checked ->
                useStub = checked
                prefs.edit().putBoolean(CalendarWidget.USE_STUB_KEY, checked).apply()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = PhosphorWhite,
                checkedTrackColor = PhosphorRed,
            ),
        )
    }
}
