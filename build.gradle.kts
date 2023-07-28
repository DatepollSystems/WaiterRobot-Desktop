import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.8.20"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion
    id("org.jetbrains.compose") version "1.4.0"
}

group = "org.datepollsystems.waiterrobot.mediator"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation(compose.materialIconsExtended)

    val ktorVersion = "2.2.3"
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

    implementation("io.insert-koin:koin-core:3.4.2")
    implementation("io.insert-koin:koin-compose:1.0.3")

    implementation("org.apache.pdfbox:pdfbox:3.0.0-RC1")
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.2") // Adds a Main Dispatcher for Desktop

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    implementation(kotlin("reflect"))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

compose.desktop {
    application {
        mainClass = "org.datepollsystems.waiterrobot.mediator.App"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb) // TODO add more?
            packageName = "WaiterRobot Desktop"
            packageVersion = (project.findProperty("versionString") as? String)?.removePrefix("v")

            macOS {
                iconFile.set(project.file("icon.icns"))
            }
            windows {
                iconFile.set(project.file("icon.ico"))
            }
            linux {
                iconFile.set(project.file("icon.png"))
            }

            includeAllModules = true // TODO figure out which modules are really needed -> reduces app size
        }
    }
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}