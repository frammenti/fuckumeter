package dev.frammenti.fuckumeter.widget.ui

import android.R
import androidx.compose.runtime.Composable
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import dev.frammenti.fuckumeter.widget.actions.ToggleActiveAction

@Composable
fun IdleView(userLevel: Float, partnerLevel: Float) {
    Box(
        modifier =
            GlanceModifier.fillMaxSize()
                .background(GlanceTheme.colors.widgetBackground)
                .cornerRadius(R.dimen.system_app_widget_background_radius)
                .clickable(actionRunCallback<ToggleActiveAction>())
    ) {
        Bar(partnerLevel, false)
        Indicator(userLevel, false)
    }
}
