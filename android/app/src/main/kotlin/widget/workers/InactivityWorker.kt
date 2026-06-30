package dev.frammenti.fuckumeter.widget.workers

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dev.frammenti.fuckumeter.widget.AppWidget
import dev.frammenti.fuckumeter.widget.WidgetConfig.INACTIVITY_TIMEOUT
import dev.frammenti.fuckumeter.widget.WidgetConfig.KEY_APP_WIDGET_ID
import dev.frammenti.fuckumeter.widget.WidgetPreferences
import java.io.IOException

class InactivityWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    private lateinit var glanceId: GlanceId
    private lateinit var prefs: Preferences

    override suspend fun doWork(): Result {
        if (!initWorker()) return Result.failure()

        if (isActive()) deactivate()

        // Not worth to add concurrency here, can be lazy
        if (isDirty()) return syncLevel()

        return Result.success()
    }

    private fun getGlanceId(): GlanceId? {
        val appWidgetId = inputData.getInt(KEY_APP_WIDGET_ID, -1)

        if (appWidgetId == -1) return null

        return GlanceAppWidgetManager(applicationContext)
            .getGlanceIdBy(appWidgetId)
    }

    private suspend fun getPrefs(): Preferences {
        return getAppWidgetState(
            applicationContext,
            PreferencesGlanceStateDefinition,
            glanceId,
        )
    }

    private suspend fun initWorker(): Boolean {
        glanceId = getGlanceId() ?: return false
        prefs = getPrefs()
        return true
    }

    private fun isActive(): Boolean {
        return prefs[WidgetPreferences.Active] ?: false
    }

    private fun isDirty(): Boolean {
        return prefs[WidgetPreferences.Dirty] ?: false
    }

    private suspend fun deactivate() {
        updateAppWidgetState(
            applicationContext,
            PreferencesGlanceStateDefinition,
            glanceId,
        ) { prefs ->
            prefs.toMutablePreferences().apply {
                this[WidgetPreferences.Active] = false
            }
        }
        // Update widget appearance
        AppWidget().update(applicationContext, glanceId)
    }

    private suspend fun clear() {
        updateAppWidgetState(
            applicationContext,
            PreferencesGlanceStateDefinition,
            glanceId,
        ) { prefs ->
            prefs.toMutablePreferences().apply {
                this[WidgetPreferences.Dirty] = false
            }
        }
    }

    private suspend fun syncLevel(): Result {
        return try {
            // TODO: Sync data with server
            clear()
            Result.success()
        } catch (e: IOException) {
            Result.retry()
        }
    }

    companion object {
        fun schedule(
            context: Context,
            glanceId: GlanceId,
        ) {
            val appWidgetId =
                GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

            DebounceScheduler.schedule<InactivityWorker>(
                context,
                uniqueName = "inactivity-$appWidgetId",
                delayMs = INACTIVITY_TIMEOUT,
                inputData = workDataOf(KEY_APP_WIDGET_ID to appWidgetId),
            )
        }
    }
}
