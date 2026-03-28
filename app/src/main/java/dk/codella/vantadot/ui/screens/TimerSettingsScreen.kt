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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.TimerPreset
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
    val presets = remember { initialSettings.timerPresets.toMutableStateList() }
    var vibration by remember { mutableStateOf(initialSettings.timerVibration) }
    var sound by remember { mutableStateOf(initialSettings.timerSound) }
    var accentIndex by remember { mutableIntStateOf(initialSettings.accentColorIndex) }
    var fontSizePreset by remember { mutableIntStateOf(initialSettings.fontSizePreset) }

    fun currentSettings() = initialSettings.copy(
        timerPresets = presets.toList(),
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
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { SectionLabel("PRESETS") }

            itemsIndexed(presets, key = { index, _ -> index }) { index, preset ->
                PresetRow(
                    preset = preset,
                    canRemove = presets.size > 2,
                    onPresetChanged = { presets[index] = it; save() },
                    onRemove = { presets.removeAt(index); save() },
                )
            }

            if (presets.size < 5) {
                item {
                    TextButton(onClick = {
                        presets.add(TimerPreset("Timer ${presets.size + 1}", 60))
                        save()
                    }) {
                        Text(
                            text = "+ ADD PRESET",
                            style = MaterialTheme.typography.labelLarge,
                            color = VantaDotGreyLight,
                        )
                    }
                }
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
private fun PresetRow(
    preset: TimerPreset,
    canRemove: Boolean,
    onPresetChanged: (TimerPreset) -> Unit,
    onRemove: () -> Unit,
) {
    val totalSeconds = preset.seconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    fun updateMinutes(newMinutes: Int) {
        val clamped = newMinutes.coerceIn(0, 60)
        val newTotal = clamped * 60 + seconds
        onPresetChanged(preset.copy(seconds = newTotal.coerceIn(5, 3600)))
    }

    fun updateSeconds(newSeconds: Int) {
        val clamped = newSeconds.coerceIn(0, 55)
        val newTotal = minutes * 60 + clamped
        onPresetChanged(preset.copy(seconds = newTotal.coerceIn(5, 3600)))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VantaDotGreyDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        // Name row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = preset.name,
                onValueChange = { if (it.length <= 16) onPresetChanged(preset.copy(name = it)) },
                textStyle = TextStyle(
                    color = VantaDotWhite,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                ),
                cursorBrush = SolidColor(VantaDotWhite),
                singleLine = true,
                modifier = Modifier.weight(1f),
            )

            if (canRemove) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(VantaDotBlack.copy(alpha = 0.4f))
                        .clickable(onClick = onRemove),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "\u00d7",
                        style = MaterialTheme.typography.bodyLarge,
                        color = VantaDotGreyLight,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Time stepper row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            // Minutes: [−] MM [+]
            StepperButton("\u2212") { updateMinutes(minutes - 1) }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "%02d".format(minutes),
                style = MaterialTheme.typography.bodyMedium,
                color = VantaDotWhite,
            )
            Spacer(modifier = Modifier.width(6.dp))
            StepperButton("+") { updateMinutes(minutes + 1) }

            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = ":",
                style = MaterialTheme.typography.bodyMedium,
                color = VantaDotGreyLight,
            )
            Spacer(modifier = Modifier.width(4.dp))

            // Seconds: [−] SS [+]
            StepperButton("\u2212") { updateSeconds(seconds - 5) }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "%02d".format(seconds),
                style = MaterialTheme.typography.bodyMedium,
                color = VantaDotWhite,
            )
            Spacer(modifier = Modifier.width(6.dp))
            StepperButton("+") { updateSeconds(seconds + 5) }
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
