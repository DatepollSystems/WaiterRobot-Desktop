package org.datepollsystems.waiterrobot.mediator.utils

import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.isAccessible

/**
 * Returns true if a lazy property reference has been initialized, or if the property is not lazy.
 */
val KProperty0<*>.isLazyInitialized: Boolean
    get() {
        // Prevent IllegalAccessException from JVM access check on private properties.
        val originalAccessLevel = isAccessible
        isAccessible = true
        // If delegate does not exist or is not Lazy, fallback to true
        val isLazyInitialized = (getDelegate() as? Lazy<*>)?.isInitialized() ?: true
        // Reset access level.
        isAccessible = originalAccessLevel
        return isLazyInitialized
    }