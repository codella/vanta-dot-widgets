package dk.codella.vantadot.metronome.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import dk.codella.vantadot.common.CircleStyle
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.common.VantaDotWidgetTheme
import dk.codella.vantadot.metronome.data.MetronomeStatus
import dk.codella.vantadot.metronome.data.MetronomeWidgetState
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.MetronomePreset

@Composable
fun MetronomeWidgetContent(
    metronomeState: MetronomeWidgetState,
    fontSizePreset: Int = 1,
    accentColorIndex: Int = 0,
    presets: List<MetronomePreset> = emptyList(),
) {
    val fontScale = FontSizePreset.fromIndex(fontSizePreset).scaleFactor
    val accent = AccentColorPreset.fromIndex(accentColorIndex)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(VantaDotWidgetTheme.CornerRadius)
            .background(VantaDotWidgetTheme.GreyDark)
            .padding(VantaDotWidgetTheme.Padding),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when (metronomeState.status) {
                MetronomeStatus.IDLE -> IdleLayout(metronomeState, fontScale, accent, presets)
                MetronomeStatus.PLAYING -> PlayingLayout(metronomeState, fontScale, accent)
            }
        }
    }
}

@Composable
private fun IdleLayout(
    state: MetronomeWidgetState,
    fontScale: Float,
    accent: AccentColorPreset,
    presets: List<MetronomePreset>,
) {
    val context = LocalContext.current
    val currentPresetName = presets.firstOrNull { it.bpm == state.bpm }?.name

    // Preset name with chevrons
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .clickable(
                    actionRunCallback<CycleMetronomePresetActionCallback>(
                        actionParametersOf(MetronomeForwardParam to false)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderChevron(context, 16f, VantaDotWidgetTheme.GreyLightArgb, pointLeft = true)
                ),
                contentDescription = "Previous preset",
                modifier = GlanceModifier.size(16.dp),
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(
                    context,
                    currentPresetName?.uppercase() ?: "CUSTOM",
                    11f * fontScale,
                    VantaDotWidgetTheme.GreyLightArgb,
                )
            ),
            contentDescription = currentPresetName ?: "Custom",
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .clickable(
                    actionRunCallback<CycleMetronomePresetActionCallback>(
                        actionParametersOf(MetronomeForwardParam to true)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderChevron(context, 16f, VantaDotWidgetTheme.GreyLightArgb, pointLeft = false)
                ),
                contentDescription = "Next preset",
                modifier = GlanceModifier.size(16.dp),
            )
        }
    }

    Spacer(modifier = GlanceModifier.height(2.dp))

    // Large BPM display
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, state.bpm.toString(), 40f * fontScale, android.graphics.Color.WHITE)
        ),
        contentDescription = "${state.bpm} BPM",
    )

    Spacer(modifier = GlanceModifier.height(2.dp))

    // BPM adjust row: [-] BPM [+]
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .cornerRadius(6.dp)
                .background(VantaDotWidgetTheme.GreyMedium)
                .clickable(
                    actionRunCallback<AdjustBpmActionCallback>(
                        actionParametersOf(MetronomeDeltaParam to -1)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, "\u2212", 14f * fontScale, android.graphics.Color.WHITE)
                ),
                contentDescription = "Decrease BPM",
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, "BPM", 11f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
            ),
            contentDescription = "BPM",
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .cornerRadius(6.dp)
                .background(VantaDotWidgetTheme.GreyMedium)
                .clickable(
                    actionRunCallback<AdjustBpmActionCallback>(
                        actionParametersOf(MetronomeDeltaParam to 1)
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, "+", 14f * fontScale, android.graphics.Color.WHITE)
                ),
                contentDescription = "Increase BPM",
            )
        }
    }

    Spacer(modifier = GlanceModifier.height(6.dp))

    // Bottom row: PLAY button + beat dots
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(accent.inProgressBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(actionRunCallback<PlayStopActionCallback>()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, "PLAY", 12f * fontScale, accent.swatchColor.toArgb())
                ),
                contentDescription = "Play",
            )
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        BeatDots(state.beatsPerBar, -1, accent)
    }
}

@Composable
private fun PlayingLayout(
    state: MetronomeWidgetState,
    fontScale: Float,
    accent: AccentColorPreset,
) {
    val context = LocalContext.current

    // Large BPM display in accent color
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, state.bpm.toString(), 40f * fontScale, accent.swatchColor.toArgb())
        ),
        contentDescription = "${state.bpm} BPM",
    )

    Spacer(modifier = GlanceModifier.height(2.dp))

    // BPM label
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, "BPM", 11f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
        ),
        contentDescription = "BPM",
    )

    Spacer(modifier = GlanceModifier.height(6.dp))

    // Bottom row: STOP button + beat dots
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(VantaDotWidgetTheme.GreyMedium)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(actionRunCallback<PlayStopActionCallback>()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, "STOP", 12f * fontScale, android.graphics.Color.WHITE)
                ),
                contentDescription = "Stop",
            )
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        BeatDots(state.beatsPerBar, state.currentBeat, accent)
    }
}

@Composable
private fun BeatDots(
    beatsPerBar: Int,
    currentBeat: Int,
    accent: AccentColorPreset,
) {
    val context = LocalContext.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until beatsPerBar) {
            if (i > 0) Spacer(modifier = GlanceModifier.width(4.dp))

            val isCurrent = i == currentBeat
            val color = if (isCurrent) accent.swatchColor.toArgb() else VantaDotWidgetTheme.GreyLightArgb
            val style = if (isCurrent) CircleStyle.FILLED else CircleStyle.HOLLOW

            Image(
                provider = ImageProvider(
                    GlanceText.renderCircle(context, 8f, color, style)
                ),
                contentDescription = if (isCurrent) "Current beat ${i + 1}" else "Beat ${i + 1}",
                modifier = GlanceModifier.size(8.dp),
            )
        }
    }
}
