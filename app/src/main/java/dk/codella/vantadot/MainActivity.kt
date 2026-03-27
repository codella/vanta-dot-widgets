package dk.codella.vantadot

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import dk.codella.vantadot.calendar.data.CalendarRepository
import dk.codella.vantadot.ui.screens.WidgetCatalogScreen
import dk.codella.vantadot.ui.theme.VantaDotTheme

class MainActivity : ComponentActivity() {

    private var hasCalendarPermission by mutableStateOf(false)
    private var hasNotificationPermission by mutableStateOf(false)

    private val calendarPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCalendarPermission = granted
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotificationPermission = granted
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        refreshPermissions()

        setContent {
            VantaDotTheme {
                WidgetCatalogScreen(
                    hasCalendarPermission = hasCalendarPermission,
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                    },
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestNotificationPermission = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissions()
    }

    private fun refreshPermissions() {
        hasCalendarPermission = CalendarRepository(this).hasCalendarPermission()
        hasNotificationPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
