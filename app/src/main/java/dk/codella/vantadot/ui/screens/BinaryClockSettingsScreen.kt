package dk.codella.vantadot.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dk.codella.vantadot.binaryclock.data.BinaryClockDotShape
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.theme.VantaDotBlack
import dk.codella.vantadot.ui.theme.VantaDotWhite

@Composable
fun BinaryClockSettingsScreen(
    initialSettings: WidgetSettings = WidgetSettings(),
    onBack: () -> Unit,
    onSettingsChanged: (WidgetSettings) -> Unit,
) {
    var showSeconds by remember { mutableStateOf(initialSettings.binaryClockShowSeconds) }
    var use24Hour by remember { mutableStateOf(initialSettings.binaryClockUse24Hour) }
    var showDigitalTime by remember { mutableStateOf(initialSettings.binaryClockShowDigitalTime) }
    var showBitLabels by remember { mutableStateOf(initialSettings.binaryClockShowBitLabels) }
    var showColumnLabels by remember { mutableStateOf(initialSettings.binaryClockShowColumnLabels) }
    var dotShape by remember { mutableIntStateOf(initialSettings.binaryClockDotShape) }
    var accentIndex by remember { mutableIntStateOf(initialSettings.binaryClockAccentColorIndex) }
    var fontSizePreset by remember { mutableIntStateOf(initialSettings.fontSizePreset) }

    fun currentSettings() = initialSettings.copy(
        binaryClockShowSeconds = showSeconds,
        binaryClockUse24Hour = use24Hour,
        binaryClockShowDigitalTime = showDigitalTime,
        binaryClockShowBitLabels = showBitLabels,
        binaryClockShowColumnLabels = showColumnLabels,
        binaryClockDotShape = dotShape,
        binaryClockAccentColorIndex = accentIndex,
        fontSizePreset = fontSizePreset,
    )

    fun save() {
        onSettingsChanged(currentSettings())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VantaDotBlack)
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(
                    text = "< BACK",
                    style = MaterialTheme.typography.labelLarge,
                    color = VantaDotWhite,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { SectionLabel("DISPLAY") }

            item {
                SettingToggle("SHOW SECONDS", showSeconds) {
                    showSeconds = it; save()
                }
            }

            item {
                SettingToggle("24-HOUR FORMAT", use24Hour) {
                    use24Hour = it; save()
                }
            }

            item {
                SettingToggle("SHOW DIGITAL TIME", showDigitalTime) {
                    showDigitalTime = it; save()
                }
            }

            item {
                SettingToggle("SHOW BIT LABELS", showBitLabels) {
                    showBitLabels = it; save()
                }
            }

            item {
                SettingToggle("SHOW COLUMN LABELS", showColumnLabels) {
                    showColumnLabels = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("DOT SHAPE") }

            item {
                SegmentedSelector(
                    options = BinaryClockDotShape.entries.map { it.displayName },
                    selectedIndex = dotShape,
                ) {
                    dotShape = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("ACCENT COLOR") }

            item {
                AccentColorRow(accentIndex) {
                    accentIndex = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("DOT SIZE") }

            item {
                SegmentedSelector(
                    options = FontSizePreset.entries.map { it.displayName },
                    selectedIndex = fontSizePreset,
                ) {
                    fontSizePreset = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}
