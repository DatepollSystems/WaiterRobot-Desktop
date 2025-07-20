import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream

plugins {
    val kotlinVersion = "1.9.23"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.compose") version "1.7.1"
    id("io.gitlab.arturbosch.detekt") version "1.23.6"
    id("dev.hydraulic.conveyor") version "1.10"
}

group = "org.datepollsystems.waiterrobot.mediator"
version = (project.findProperty("versionString") as? String)?.removePrefix("v")
    ?: System.getenv("WAITERROBOT_VERSION_STRING")?.removePrefix("v")?.ifEmpty { null }
    ?: "99.99.99"

repositories {
    google()
    mavenCentral()
}

dependencies {
    linuxAmd64(compose.desktop.linux_x64)
    linuxAarch64(compose.desktop.linux_arm64)
    macAmd64(compose.desktop.macos_x64)
    macAarch64(compose.desktop.macos_arm64)
    windowsAmd64(compose.desktop.windows_x64)

    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)

    val ktorVersion = "2.3.11"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    val mokoMvvmVersion = "0.16.1"
    implementation("dev.icerock.moko:mvvm-core:$mokoMvvmVersion")
    implementation("dev.icerock.moko:mvvm-compose:$mokoMvvmVersion")

    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.insert-koin:koin-compose:1.1.5")

    val kermitVersion = "2.0.3"
    implementation("co.touchlab:kermit:$kermitVersion")
    implementation("io.sentry:sentry:7.9.0")

    implementation("org.apache.pdfbox:pdfbox:3.0.2")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.1") // Adds a Main Dispatcher for Desktop

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
    testImplementation("co.touchlab:kermit-test:$kermitVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

compose.desktop {
    application {
        mainClass = "org.datepollsystems.waiterrobot.mediator.App"
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
    languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
}

val detektReportMergeSarif by tasks.registering(ReportMergeTask::class) {
    output = layout.buildDirectory.file("reports/detekt/merge.sarif")
}

detekt {
    config.from(rootDir.resolve("detekt.yml"))
    buildUponDefaultConfig = true
    basePath = rootDir.path
    // Autocorrection can only be done locally
    autoCorrect = System.getenv("CI")?.lowercase() != true.toString()
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required = true
        sarif.required = true
    }
    finalizedBy(detektReportMergeSarif)
}
detektReportMergeSarif {
    input.from(tasks.withType<Detekt>().map { it.sarifReportFile })
}

// Work around temporary Compose bugs.
configurations.all {
    attributes {
        // https://github.com/JetBrains/compose-jb/issues/1404#issuecomment-1146894731
        attribute(Attribute.of("ui", String::class.java), "awt")
    }
}

tasks.register("release") {
    val versionParam = findProperty("v")?.toString()

    doLast {
        val versionTag = "v" + getNewVersion(versionParam)

        println()
        println("Creating git tag $versionTag")
        exec {
            commandLine("git", "tag", versionTag)
        }

        println("Push git tag $versionTag to origin")
        exec {
            commandLine("git", "push", "origin", versionTag)
        }
    }
}

fun getNewVersion(version: String?): VersionNumber {
    val lastTagVersion = getLastTag()

    val newVersion = if (version != null) {
        VersionNumber.fromString(version)
    } else {
        println(
            """
            The latest version tag is: $lastTagVersion
            What do you want to increase?
              1): Major
              2): Minor
              3): Patch (default)
            """.trimIndent()
        )

        when (readln()) {
            "1" -> lastTagVersion.nextMajor()
            "2" -> lastTagVersion.nextMinor()
            else -> lastTagVersion.nextPatch()
        }
    }

    require(newVersion > lastTagVersion) {
        "New version ($newVersion) must be grater than the latest version ($lastTagVersion)."
    }

    return newVersion
}

fun getLastTag(): VersionNumber {
    // Fetch all the remote tags
    exec {
        commandLine("git", "fetch", "--tags")
    }

    // Capture the names of all tags
    val osAllTags = ByteArrayOutputStream()
    exec {
        commandLine("git", "tag", "-l")
        standardOutput = osAllTags
    }
    val allTags: List<String> = osAllTags.toString(Charsets.UTF_8)
        .trim()
        .split("\n")
        .filter { it.matches(Regex("""v\d+\.\d+\.\d+""")) }
        .map { it.removePrefix("v") }

    return VersionNumber.fromString(allTags.last())
}

data class VersionNumber(val major: Int, val minor: Int, val patch: Int) {
    override fun toString(): String = "$major.$minor.$patch"

    fun nextMajor() = VersionNumber(major + 1, 0, 0)

    fun nextMinor() = VersionNumber(major, minor + 1, 0)

    fun nextPatch() = VersionNumber(major, minor, patch + 1)

    operator fun compareTo(other: VersionNumber): Int {
        return when {
            this.major != other.major -> this.major.compareTo(other.major)
            this.minor != other.minor -> this.minor.compareTo(other.minor)
            else -> this.patch.compareTo(other.patch)
        }
    }

    companion object {
        fun fromString(version: String): VersionNumber {
            val split = version.removePrefix("v").split('.')
            require(split.count() == 3) {
                "The provided version '$version' is not valid. It must follow the pattern of x.y.z (e.g. 1.2.3)"
            }

            return VersionNumber(
                major = split[0].toInt(),
                minor = split[1].toInt(),
                patch = split[2].toInt()
            )
        }
    }
}
