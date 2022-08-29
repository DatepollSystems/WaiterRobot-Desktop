import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
}

group = "org.datepollsystems.waiterrobot.mediator"
version = "1.0.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs) // TODO how to build for multiple platforms
    implementation(compose.materialIconsExtended)

    val ktorVersion = "2.0.3"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-auth:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("io.ktor:ktor-client-websockets:$ktorVersion")

    implementation("org.apache.pdfbox:pdfbox:3.0.0-alpha3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Mediator"
            packageVersion = "1.0.0"
        }
    }
}

// TODO probably can be removed when cross-compilation is supported
tasks.withType(Jar::class) {
    manifest {
        attributes["Manifest-Version"] = "1.0.0" // TODO use version variable
        attributes["Main-Class"] = "org.datepollsystems.waiterrobot.mediator.App"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    // To add all the dependencies
    from(sourceSets.main.get().output)

    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
}

kotlin.sourceSets.all {
    languageSettings.optIn("kotlin.RequiresOptIn")
}

tasks.getByPath(":prepareKotlinBuildScriptModel").dependsOn("addKtlintFormatGitPreCommitHook")
ktlint {
    // Plugin currently uses ktlint version "0.43.2" but with "0.43.0" a bug was introduced that removes used wildcard imports.
    // Bug was fixed with ktlint "0.45.0"
    // -> Use the fixed ktlint version (0.46.0 introduces breaking changes)
    // see https://github.com/pinterest/ktlint/issues/1256, https://github.com/pinterest/ktlint/issues/1277, https://github.com/pinterest/ktlint/issues/1393, https://github.com/pinterest/ktlint/pull/1402
    version.set("0.45.2")
}