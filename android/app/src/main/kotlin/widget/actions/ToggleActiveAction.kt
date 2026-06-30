package dev.frammenti.fuckumeter.widget.actions

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dev.frammenti.fuckumeter.widget.AppWidget
import dev.frammenti.fuckumeter.widget.WidgetPreferences
import dev.frammenti.fuckumeter.widget.workers.InactivityWorker

class ToggleActiveAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        activate(context, glanceId)
        InactivityWorker.schedule(context, glanceId)
    }

    private suspend fun activate(
        context: Context,
        glanceId: GlanceId,
    ) {
        updateAppWidgetState(
            context,
            PreferencesGlanceStateDefinition,
            glanceId,
        ) { prefs ->
            prefs.toMutablePreferences().apply {
                this[WidgetPreferences.Active] = true
            }
        }
        // Update widget appearance
        AppWidget().update(context, glanceId)
    }
}
