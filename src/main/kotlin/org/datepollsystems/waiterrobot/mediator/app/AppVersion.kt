package org.datepollsystems.waiterrobot.mediator.app

data class AppVersion(val major: Int, val minor: Int, val patch: Int) {

    operator fun compareTo(other: AppVersion): Int {
        if (this.major != other.major) return this.major.compareTo(other.major)
        if (this.minor != other.minor) return this.minor.compareTo(other.minor)
        return this.patch.compareTo(other.patch)
    }

    override fun toString(): String = "$major.$minor.$patch"

    companion object {
        fun fromVersionString(version: String): AppVersion {
            val versionRegex = Regex("""^v?(\d+)\.(\d+)\.(\d+)$""")
            require(version.matches(versionRegex))
            val (major, minor, patch) = versionRegex.find(version)!!.destructured.toList().map(String::toInt)
            return AppVersion(major, minor, patch)
        }

        fun fromVersionStringOrNull(version: String): AppVersion? =
            runCatching { fromVersionString(version) }.getOrNull()

        val current: AppVersion = fromVersionString(System.getProperty("jpackage.app-version"))
    }
}
