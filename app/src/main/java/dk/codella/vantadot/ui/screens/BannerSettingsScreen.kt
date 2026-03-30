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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import dk.codella.vantadot.banner.data.BannerScrollDirection
import dk.codella.vantadot.banner.data.BannerVibe
import dk.codella.vantadot.settings.BannerMessageEntry
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.theme.VantaDotBlack
import dk.codella.vantadot.ui.theme.VantaDotGreyDark
import dk.codella.vantadot.ui.theme.VantaDotGreyLight
import dk.codella.vantadot.ui.theme.VantaDotWhite

@Composable
fun BannerSettingsScreen(
    initialSettings: WidgetSettings = WidgetSettings(),
    onBack: () -> Unit,
    onSettingsChanged: (WidgetSettings) -> Unit,
) {
    val messages = remember { initialSettings.bannerMessages.toMutableStateList() }
    var vibe by remember { mutableIntStateOf(initialSettings.bannerVibe) }
    var scrollSpeed by remember { mutableIntStateOf(initialSettings.bannerScrollSpeed) }
    var scrollDirection by remember { mutableIntStateOf(initialSettings.bannerScrollDirection) }
    var gapSeconds by remember { mutableIntStateOf(initialSettings.bannerGapSeconds) }
    var accentIndex by remember { mutableIntStateOf(initialSettings.bannerAccentColorIndex) }
    var fontSizePreset by remember { mutableIntStateOf(initialSettings.bannerFontSizePreset) }

    fun currentSettings() = initialSettings.copy(
        bannerMessages = messages.toList(),
        bannerVibe = vibe,
        bannerScrollSpeed = scrollSpeed,
        bannerScrollDirection = scrollDirection,
        bannerGapSeconds = gapSeconds,
        bannerAccentColorIndex = accentIndex,
        bannerFontSizePreset = fontSizePreset,
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
            Spacer(modifier = Modifier.weight(1f))
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
            item { SectionLabel("MESSAGES") }

            itemsIndexed(messages, key = { index, _ -> index }) { index, message ->
                MessageRow(
                    message = message,
                    canRemove = messages.size > 1,
                    onMessageChanged = { messages[index] = it; save() },
                    onRemove = { messages.removeAt(index); save() },
                )
            }

            if (messages.size < 10) {
                item {
                    TextButton(onClick = {
                        messages.add(BannerMessageEntry(""))
                        save()
                    }) {
                        Text(
                            text = "+ ADD MESSAGE",
                            style = MaterialTheme.typography.labelLarge,
                            color = VantaDotGreyLight,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("VIBE") }

            item {
                SegmentedSelector(
                    options = BannerVibe.entries.map { it.displayName },
                    selectedIndex = vibe,
                ) {
                    vibe = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("SCROLL SPEED") }

            item {
                Slider(
                    value = scrollSpeed.toFloat(),
                    onValueChange = { scrollSpeed = it.toInt(); save() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = VantaDotWhite,
                        activeTrackColor = VantaDotWhite,
                        inactiveTrackColor = VantaDotGreyDark,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            item { SectionLabel("DIRECTION") }

            item {
                SegmentedSelector(
                    options = BannerScrollDirection.entries.map { it.displayName },
                    selectedIndex = scrollDirection,
                ) {
                    scrollDirection = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("GAP BETWEEN MESSAGES — ${gapSeconds}S") }

            item {
                Slider(
                    value = gapSeconds.toFloat(),
                    onValueChange = { gapSeconds = it.toInt(); save() },
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = VantaDotWhite,
                        activeTrackColor = VantaDotWhite,
                        inactiveTrackColor = VantaDotGreyDark,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
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
private fun MessageRow(
    message: BannerMessageEntry,
    canRemove: Boolean,
    onMessageChanged: (BannerMessageEntry) -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VantaDotGreyDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = message.text,
            onValueChange = { if (it.length <= 100) onMessageChanged(BannerMessageEntry(it)) },
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
}
