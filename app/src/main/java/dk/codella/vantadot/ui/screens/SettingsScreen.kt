package dk.codella.vantadot.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dk.codella.vantadot.BuildConfig
import dk.codella.vantadot.calendar.data.CalendarInfo
import dk.codella.vantadot.calendar.data.CalendarRepository
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.theme.VantaDotBlack
import dk.codella.vantadot.ui.theme.VantaDotGreyDark
import dk.codella.vantadot.ui.theme.VantaDotGreyLight
import dk.codella.vantadot.ui.theme.VantaDotWhite

@Composable
fun SettingsScreen(
    hasCalendarPermission: Boolean,
    initialSettings: WidgetSettings = WidgetSettings(),
    onBack: () -> Unit,
    onSettingsChanged: (WidgetSettings) -> Unit,
) {
    val context = LocalContext.current

    var showHeader by remember { mutableStateOf(initialSettings.showSectionHeader) }
    var showAllDay by remember { mutableStateOf(initialSettings.showAllDayEvents) }
    var showLocation by remember { mutableStateOf(initialSettings.showEventLocation) }
    var showTentative by remember { mutableStateOf(initialSettings.showTentativeEvents) }
    var maxEvents by remember { mutableFloatStateOf(initialSettings.maxEvents.toFloat()) }
    var accentIndex by remember { mutableIntStateOf(initialSettings.accentColorIndex) }
    var includedIds by remember { mutableStateOf(initialSettings.includedCalendarIds) }
    var calendars by remember { mutableStateOf<List<CalendarInfo>>(emptyList()) }
    var use24Hour by remember { mutableStateOf(initialSettings.use24HourFormat) }
    var compactTime by remember { mutableStateOf(initialSettings.showCompactTime) }
    var fontSizePreset by remember { mutableIntStateOf(initialSettings.fontSizePreset) }
    var useStubData by remember { mutableStateOf(initialSettings.useStubData) }

    LaunchedEffect(hasCalendarPermission) {
        if (hasCalendarPermission) {
            calendars = CalendarRepository(context).getCalendars()
        }
    }

    fun currentSettings() = WidgetSettings(
        showSectionHeader = showHeader,
        showAllDayEvents = showAllDay,
        showEventLocation = showLocation,
        showTentativeEvents = showTentative,
        maxEvents = maxEvents.toInt(),
        accentColorIndex = accentIndex,
        includedCalendarIds = includedIds,
        use24HourFormat = use24Hour,
        showCompactTime = compactTime,
        fontSizePreset = fontSizePreset,
        useStubData = useStubData,
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
                SettingToggle("SHOW HEADER", showHeader) {
                    showHeader = it; save()
                }
            }

            item {
                SettingToggle("SHOW ALL-DAY EVENTS", showAllDay) {
                    showAllDay = it; save()
                }
            }

            item {
                SettingToggle("SHOW EVENT LOCATION", showLocation) {
                    showLocation = it; save()
                }
            }

            item {
                SettingToggle("SHOW TENTATIVE EVENTS", showTentative) {
                    showTentative = it; save()
                }
            }

            item {
                SettingToggle("24-HOUR TIME FORMAT", use24Hour) {
                    use24Hour = it; save()
                }
            }

            item {
                SettingToggle("COMPACT TIME (START ONLY)", compactTime) {
                    compactTime = it; save()
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("MAX EVENTS") }

            item {
                MaxEventsSlider(maxEvents) {
                    maxEvents = it; save()
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

            item { Spacer(modifier = Modifier.height(12.dp)) }

            item { SectionLabel("CALENDARS") }

            if (!hasCalendarPermission) {
                item {
                    Text(
                        text = "Grant calendar permission to select calendars",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VantaDotGreyLight,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else if (calendars.isEmpty()) {
                item {
                    Text(
                        text = "No calendars found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = VantaDotGreyLight,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            } else {
                item {
                    val allSelected = includedIds.isEmpty()
                    SettingToggle("ALL CALENDARS", allSelected) {
                        includedIds = if (it) emptySet() else calendars.map { c -> c.id }.toSet()
                        save()
                    }
                }

                items(calendars, key = { it.id }) { calendar ->
                    val isIncluded = includedIds.isEmpty() || calendar.id in includedIds
                    CalendarRow(calendar, isIncluded) { checked ->
                        includedIds = if (checked) {
                            if (includedIds.isEmpty()) {
                                calendars.map { it.id }.toSet()
                            } else {
                                includedIds + calendar.id
                            }
                        } else {
                            val currentIds = if (includedIds.isEmpty()) {
                                calendars.map { it.id }.toSet()
                            } else {
                                includedIds
                            }
                            val newIds = currentIds - calendar.id
                            if (newIds.size == calendars.size) emptySet() else newIds
                        }
                        save()
                    }
                }
            }

            if (BuildConfig.DEBUG) {
                item { Spacer(modifier = Modifier.height(12.dp)) }

                item { SectionLabel("DEBUG") }

                item {
                    SettingToggle("USE STUB DATA", useStubData) {
                        useStubData = it; save()
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = VantaDotGreyLight,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@Composable
private fun SettingToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
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
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = VantaDotBlack,
                checkedTrackColor = VantaDotWhite,
                uncheckedThumbColor = VantaDotGreyLight,
                uncheckedTrackColor = VantaDotGreyDark,
            ),
        )
    }
}

@Composable
private fun MaxEventsSlider(value: Float, onValueChange: (Float) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(VantaDotGreyDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "SHOW UP TO",
                style = MaterialTheme.typography.bodyMedium,
                color = VantaDotWhite,
            )
            Text(
                text = "${value.toInt()} EVENTS",
                style = MaterialTheme.typography.bodyMedium,
                color = VantaDotWhite,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 2f..20f,
            steps = 17,
            colors = SliderDefaults.colors(
                thumbColor = VantaDotWhite,
                activeTrackColor = VantaDotWhite,
                inactiveTrackColor = VantaDotGreyLight.copy(alpha = 0.3f),
            ),
        )
    }
}

@Composable
private fun AccentColorRow(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VantaDotGreyDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AccentColorPreset.entries.forEachIndexed { index, preset ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(preset.swatchColor, CircleShape)
                    .then(
                        if (isSelected) Modifier.border(2.dp, VantaDotWhite, CircleShape)
                        else Modifier
                    )
                    .clickable { onSelect(index) },
            )
        }
    }
}

@Composable
private fun SegmentedSelector(options: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VantaDotGreyDark, RoundedCornerShape(12.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) VantaDotWhite else VantaDotBlack.copy(alpha = 0.3f))
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) VantaDotBlack else VantaDotGreyLight,
                )
            }
        }
    }
}

@Composable
private fun CalendarRow(calendar: CalendarInfo, isIncluded: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(VantaDotGreyDark, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(androidx.compose.ui.graphics.Color(calendar.color), CircleShape),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = calendar.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = VantaDotWhite,
            )
            Text(
                text = calendar.accountName,
                style = MaterialTheme.typography.bodySmall,
                color = VantaDotGreyLight,
            )
        }
        Checkbox(
            checked = isIncluded,
            onCheckedChange = onToggle,
            colors = CheckboxDefaults.colors(
                checkedColor = VantaDotWhite,
                uncheckedColor = VantaDotGreyLight,
                checkmarkColor = VantaDotBlack,
            ),
        )
    }
}
