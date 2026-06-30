package dev.frammenti.fuckumeter.widget.ui

import android.R
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize

@Composable
fun ActiveView(userLevel: Float, partnerLevel: Float) {
    Box(
        modifier =
            GlanceModifier.fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(R.dimen.system_app_widget_background_radius)
    ) {
        Bar(partnerLevel, true)
        Indicator(userLevel, true)
        Controls()
    }
}
