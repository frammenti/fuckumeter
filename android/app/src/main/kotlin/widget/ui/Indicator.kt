package dev.frammenti.fuckumeter.widget.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import dev.frammenti.fuckumeter.R

@Composable
fun Indicator(userLevel: Float, active: Boolean) {
    Row(
        modifier =
            GlanceModifier.fillMaxSize().padding(start = 4.dp, end = 4.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(
            GlanceModifier.width(
                ((LocalSize.current.width - 8.dp) * userLevel - 15.dp).coerceIn(
                    0.dp,
                    LocalSize.current.width - (30 + 4).dp,
                )
            )
        )

        Column(
            modifier = GlanceModifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.Bottom,
        ) {
            if (active) {
                Text(
                    (userLevel * 100).toInt().toString(),
                    style =
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = GlanceTheme.colors.onSurface,
                        ),
                    maxLines = 1,
                )
            }

            Image(
                provider = ImageProvider(R.drawable.ic_arrow_down),
                contentDescription = "Indicator",
                colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurface),
            )
        }
    }
}
