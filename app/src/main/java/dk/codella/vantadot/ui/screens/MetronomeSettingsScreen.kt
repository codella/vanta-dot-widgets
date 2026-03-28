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
import dk.codella.vantadot.metronome.data.MetronomeSoundChoice
import dk.codella.vantadot.metronome.data.MetronomeWidgetState
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.MetronomePreset
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.theme.VantaDotBlack
import dk.codella.vantadot.ui.theme.VantaDotGreyDark
import dk.codella.vantadot.ui.theme.VantaDotGreyLight
import dk.codella.vantadot.ui.theme.VantaDotWhite

private val TIME_SIGNATURES = listOf(
    "2/4" to 2,
    "3/4" to 3,
    "4/4" to 4,
    "5/4" to 5,
    "6/8" to 6,
    "7/8" to 7,
)

@Composable
fun MetronomeSettingsScreen(
    initialSettings: WidgetSettings = WidgetSettings(),
    onBack: () -> Unit,
    onSettingsChanged: (WidgetSettings) -> Unit,
) {
    val presets = remember { initialSettings.metronomePresets.toMutableStateList() }
    var beatsPerBar by remember { mutableIntStateOf(initialSettings.metronomeBeatsPerBar) }
    var soundChoice by remember { mutableIntStateOf(initialSettings.metronomeSoundChoice) }
    var accentFirstBeat by remember { mutableStateOf(initialSettings.metronomeAccentFirstBeat) }
    var vibration by remember { mutableStateOf(initialSettings.metronomeVibration) }
    var accentIndex by remember { mutableIntStateOf(initialSettings.accentColorIndex) }
    var fontSizePreset by remember { mutableIntStateOf(initialSettings.fontSizePreset) }

    fun currentSettings() = initialSettings.copy(
        metronomePresets = presets.toList(),
        metronomeBeatsPerBar = beatsPerBar,
        metronomeSoundChoice = soundChoice,
        metronomeAccentFirstBeat = accentFirstBeat,
        metronomeVibration = vibration,
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
            item { SectionLabel("PRESETS") }

            itemsIndexed(presets, key = { index, _ -> index }) { index, preset ->
                MetronomePresetRow(
                    preset = preset,
                    canRemove = presets.size > 2,
                    onPresetChanged = { presets[index] = it; save() },
                    onRemove = { presets.removeAt(index); save() },
                )
            }

            if (presets.size < 5) {
                item {
                    TextButton(onClick = {
                        presets.add(MetronomePreset("Preset ${presets.size + 1}", 120))
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

            item { SectionLabel("TIME SIGNATURE") }

            item {
                SegmentedSelector(
                    options = TIME_SIGNATURES.map { it.first },
                    selectedIndex = TIME_SIGNATURES.indexOfFirst { it.second == beatsPerBar }.coerceAtLeast(0),
                ) { index ->
                    beatsPerBar = TIME_SIGNATURES[index].second
                    save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("SOUND") }

            item {
                SegmentedSelector(
                    options = MetronomeSoundChoice.entries.map { it.displayName },
                    selectedIndex = soundChoice,
                ) {
                    soundChoice = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("FEEDBACK") }

            item {
                SettingToggle("ACCENT FIRST BEAT", accentFirstBeat) {
                    accentFirstBeat = it; save()
                }
            }

            item {
                SettingToggle("VIBRATION", vibration) {
                    vibration = it; save()
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
private fun MetronomePresetRow(
    preset: MetronomePreset,
    canRemove: Boolean,
    onPresetChanged: (MetronomePreset) -> Unit,
    onRemove: () -> Unit,
) {
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

        // BPM stepper row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            BpmStepperButton("\u2212") {
                val newBpm = (preset.bpm - 1).coerceIn(MetronomeWidgetState.MIN_BPM, MetronomeWidgetState.MAX_BPM)
                onPresetChanged(preset.copy(bpm = newBpm))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${preset.bpm}",
                style = MaterialTheme.typography.bodyMedium,
                color = VantaDotWhite,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "BPM",
                style = MaterialTheme.typography.bodySmall,
                color = VantaDotGreyLight,
            )
            Spacer(modifier = Modifier.width(8.dp))
            BpmStepperButton("+") {
                val newBpm = (preset.bpm + 1).coerceIn(MetronomeWidgetState.MIN_BPM, MetronomeWidgetState.MAX_BPM)
                onPresetChanged(preset.copy(bpm = newBpm))
            }
        }
    }
}

@Composable
private fun BpmStepperButton(text: String, onClick: () -> Unit) {
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
