import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.9.0"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.compose") version "1.4.3"
    id("io.gitlab.arturbosch.detekt") version "1.23.1"
    id("dev.hydraulic.conveyor") version "1.6"
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

    implementation(compose.materialIconsExtended)

    val ktorVersion = "2.3.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    val mokoMvvmVersion = "0.16.1"
    implementation("dev.icerock.moko:mvvm-core:${mokoMvvmVersion}")
    implementation("dev.icerock.moko:mvvm-compose:${mokoMvvmVersion}")

    implementation("io.insert-koin:koin-core:3.4.3")
    implementation("io.insert-koin:koin-compose:1.0.4")

    val kermitVersion = "2.0.0-RC5"
    implementation("co.touchlab:kermit:$kermitVersion")
    implementation("io.sentry:sentry:6.27.0")

    implementation("org.apache.pdfbox:pdfbox:3.0.0-RC1")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3") // Adds a Main Dispatcher for Desktop

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation(kotlin("reflect"))

    testImplementation(kotlin("test"))
    testImplementation("co.touchlab:kermit-test:$kermitVersion")

    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.1")
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
