package dev.frammenti.fuckumeter.widget.ui

import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize

@Composable
fun Bar(partnerLevel: Float, active: Boolean) {
    Row(modifier = GlanceModifier.fillMaxSize()) {
        LinearProgressIndicator(
            progress = partnerLevel,
            modifier = GlanceModifier.fillMaxSize(),
        )
    }
}
