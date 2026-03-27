package dk.codella.vantadot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.theme.VantaDotBlack
import dk.codella.vantadot.ui.theme.VantaDotGreyDark
import dk.codella.vantadot.ui.theme.VantaDotGreyLight
import dk.codella.vantadot.ui.theme.VantaDotWhite

@Composable
fun TimerSettingsScreen(
    initialSettings: WidgetSettings = WidgetSettings(),
    onBack: () -> Unit,
    onSettingsChanged: (WidgetSettings) -> Unit,
) {
    var preset1 by remember { mutableIntStateOf(initialSettings.timerPreset1Minutes) }
    var preset2 by remember { mutableIntStateOf(initialSettings.timerPreset2Minutes) }
    var preset3 by remember { mutableIntStateOf(initialSettings.timerPreset3Minutes) }
    var preset4 by remember { mutableIntStateOf(initialSettings.timerPreset4Minutes) }
    var vibration by remember { mutableStateOf(initialSettings.timerVibration) }
    var sound by remember { mutableStateOf(initialSettings.timerSound) }
    var accentIndex by remember { mutableIntStateOf(initialSettings.accentColorIndex) }
    var fontSizePreset by remember { mutableIntStateOf(initialSettings.fontSizePreset) }

    fun currentSettings() = initialSettings.copy(
        timerPreset1Minutes = preset1,
        timerPreset2Minutes = preset2,
        timerPreset3Minutes = preset3,
        timerPreset4Minutes = preset4,
        timerVibration = vibration,
        timerSound = sound,
        accentColorIndex = accentIndex,
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
                text = "TIMER SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { SectionLabel("PRESETS") }

            item {
                PresetStepper("PRESET 1", preset1) { preset1 = it; save() }
            }

            item {
                PresetStepper("PRESET 2", preset2) { preset2 = it; save() }
            }

            item {
                PresetStepper("PRESET 3", preset3) { preset3 = it; save() }
            }

            item {
                PresetStepper("PRESET 4", preset4) { preset4 = it; save() }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("COMPLETION") }

            item {
                SettingToggle("VIBRATION", vibration) {
                    vibration = it; save()
                }
            }

            item {
                SettingToggle("SOUND", sound) {
                    sound = it; save()
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

            item { SectionLabel("FONT SIZE") }

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

@Composable
private fun PresetStepper(label: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VantaDotGreyDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = VantaDotWhite,
            modifier = Modifier.weight(1f),
        )

        StepperButton("\u2212") {
            if (value > 1) onValueChange(value - 1)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "${value} MIN",
            style = MaterialTheme.typography.bodyMedium,
            color = VantaDotWhite,
        )

        Spacer(modifier = Modifier.width(12.dp))

        StepperButton("+") {
            if (value < 60) onValueChange(value + 1)
        }
    }
}

@Composable
private fun StepperButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(VantaDotBlack.copy(alpha = 0.4f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = VantaDotWhite,
        )
    }
}