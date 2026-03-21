package dk.codella.phosphor.ui.screens

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.codella.phosphor.ui.theme.PhosphorGreyDark
import dk.codella.phosphor.ui.theme.PhosphorRed
import dk.codella.phosphor.ui.theme.PhosphorWhite

@Composable
fun WidgetPreviewCard(
    widgetName: String,
    widgetDescription: String,
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    receiverClass: Class<*>,
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PhosphorGreyDark),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = widgetName,
                style = MaterialTheme.typography.titleLarge,
                color = PhosphorWhite,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = widgetDescription,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!hasPermission) {
                    TextButton(
                        onClick = onRequestPermission,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = PhosphorRed,
                        ),
                    ) {
                        Text("GRANT PERMISSION")
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Button(
                    onClick = { requestPinWidget(context, receiverClass) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PhosphorWhite,
                        contentColor = PhosphorGreyDark,
                    ),
                ) {
                    Text(
                        text = "PIN WIDGET",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

private fun requestPinWidget(context: Context, receiverClass: Class<*>) {
    val appWidgetManager = AppWidgetManager.getInstance(context)
    val provider = ComponentName(context, receiverClass)
    if (appWidgetManager.isRequestPinAppWidgetSupported) {
        appWidgetManager.requestPinAppWidget(provider, null, null)
    }
}
