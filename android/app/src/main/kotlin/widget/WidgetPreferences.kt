package dev.frammenti.fuckumeter.widget

import androidx.datastore.preferences.core.booleanPreferencesKey

object WidgetPreferences {
    val Active = booleanPreferencesKey("active")
    val Dirty = booleanPreferencesKey("dirty")
}
