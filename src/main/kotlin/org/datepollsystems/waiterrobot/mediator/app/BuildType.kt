package org.datepollsystems.waiterrobot.mediator.app

enum class BuildType {
    DEBUG, RELEASE;

    companion object {
        fun fromProperty(): BuildType {
            return when (System.getProperty("app.build.type")?.lowercase()) {
                "debug" -> DEBUG
                "release" -> RELEASE
                else -> DEBUG
            }
        }
    }
}
