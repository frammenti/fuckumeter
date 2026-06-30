package dev.frammenti.fuckumeter.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.layout.Alignment
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import dev.frammenti.fuckumeter.R

@Composable
fun Controls() {
    Row(
        modifier =
            GlanceModifier.fillMaxSize().padding(start = 4.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_minus),
            contentDescription = "Decrease",
            onClick = {},
            backgroundColor = null,
        )

        Spacer(GlanceModifier.defaultWeight())

        CircleIconButton(
            imageProvider = ImageProvider(R.drawable.ic_plus),
            contentDescription = "Increase",
            onClick = {},
            backgroundColor = null,
        )
    }
}
