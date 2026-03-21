package dk.codella.nothingwidgets

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dk.codella.nothingwidgets.calendar.data.CalendarRepository
import dk.codella.nothingwidgets.ui.screens.WidgetCatalogScreen
import dk.codella.nothingwidgets.ui.theme.NothingTheme

class MainActivity : ComponentActivity() {

    private var hasCalendarPermission by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCalendarPermission = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        hasCalendarPermission = CalendarRepository(this).hasCalendarPermission()

        setContent {
            NothingTheme {
                WidgetCatalogScreen(
                    hasCalendarPermission = hasCalendarPermission,
                    onRequestPermission = {
                        permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasCalendarPermission = CalendarRepository(this).hasCalendarPermission()
    }
}
