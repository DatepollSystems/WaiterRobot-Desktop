package org.datepollsystems.waiterrobot.mediator.app

import java.util.prefs.Preferences
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun Preferences.nullableString(key: String? = null): ReadWriteProperty<Any?, String?> =
    PreferencesNullableStringDelegate(this, key)

private class PreferencesNullableStringDelegate(
    private val preferences: Preferences,
    private val key: String?,
) : ReadWriteProperty<Any?, String?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String? {
        preferences.sync()
        return preferences.get(key ?: property.name, null)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?) {
        if (value == null) {
            preferences.remove(key ?: property.name)
        } else {
            preferences.put(key ?: property.name, value)
        }

        preferences.flush()
    }
}