package dev.frammenti.fuckumeter.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import dev.frammenti.fuckumeter.widget.ui.ActiveView
import dev.frammenti.fuckumeter.widget.ui.IdleView

val userLevel = 0.83f
val partnerLevel = 0.52f

class AppWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Exact

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {

        // In this method, load data needed to render the AppWidget.
        // Use `withContext` to switch to another thread for long-running
        // operations.

        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val active = prefs[WidgetPreferences.Active] ?: false

        Column(
            modifier =
                GlanceModifier.fillMaxSize()
                    .padding(top = 12.dp, bottom = 12.dp)
        ) {
            if (active) ActiveView(userLevel, partnerLevel)
            else IdleView(userLevel, partnerLevel)
        }
    }
}
