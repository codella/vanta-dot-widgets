package dk.codella.vantadot.ui.screens

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
import dk.codella.vantadot.BuildConfig
import dk.codella.vantadot.calendar.widget.CalendarWidget
import dk.codella.vantadot.calendar.widget.CalendarWidgetReceiver
import dk.codella.vantadot.ui.theme.VantaDotBlack
import dk.codella.vantadot.ui.theme.VantaDotRed
import dk.codella.vantadot.ui.theme.VantaDotWhite

@Composable
fun WidgetCatalogScreen(
    hasCalendarPermission: Boolean,
    onRequestPermission: () -> Unit,
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
            color = VantaDotWhite,
            modifier = Modifier.weight(1f),
        )
        Switch(
            checked = useStub,
            onCheckedChange = { checked ->
                useStub = checked
                prefs.edit().putBoolean(CalendarWidget.USE_STUB_KEY, checked).apply()
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = VantaDotWhite,
                checkedTrackColor = VantaDotRed,
            ),
        )
    }
}
