import net.sf.image4j.codec.ico.ICOEncoder
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.io.File
import javax.imageio.ImageIO

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jclarion:image4j:0.7")
    }
}

val appVersion = providers.gradleProperty("appVersion")
    .orElse(providers.environmentVariable("APP_VERSION"))
    .orElse("1.0.0")
    .get()

val logoFile = layout.projectDirectory.file("logo.png").asFile
val generatedIconsDir = layout.buildDirectory.dir("generated/icons")
val macIconRegularFile = layout.buildDirectory.file("generated/icons/logo.icns")
val windowsIconRegularFile = layout.buildDirectory.file("generated/icons/logo.ico")

plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.compose") version "2.1.10"
    id("org.jetbrains.compose") version "1.7.3"
}

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.1")
    implementation("org.apache.tika:tika-core:2.9.2")
    implementation("org.apache.tika:tika-parsers-standard-package:2.9.2")
    implementation("org.jsoup:jsoup:1.18.3")

    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(21)
}

val generateWindowsIcon = tasks.register("generateWindowsIcon") {
    inputs.file(logoFile)
    outputs.file(windowsIconRegularFile)

    doLast {
        val outputFile = windowsIconRegularFile.get().asFile
        outputFile.parentFile.mkdirs()
        val image = ImageIO.read(logoFile)
        outputFile.outputStream().use { outputStream ->
            ICOEncoder.write(image, outputStream)
        }
    }
}

val generateMacIcon = tasks.register("generateMacIcon") {
    inputs.file(logoFile)
    outputs.file(macIconRegularFile)
    onlyIf { System.getProperty("os.name").contains("Mac", ignoreCase = true) }

    doLast {
        val iconRoot = generatedIconsDir.get().asFile
        val iconsetDir = File(iconRoot, "logo.iconset")
        val outputFile = macIconRegularFile.get().asFile

        iconsetDir.deleteRecursively()
        iconsetDir.mkdirs()
        outputFile.parentFile.mkdirs()

        val sizes = listOf(16, 32, 128, 256, 512)
        sizes.forEach { size ->
            exec {
                commandLine(
                    "sips",
                    "-z", size.toString(), size.toString(),
                    logoFile.absolutePath,
                    "--out",
                    File(iconsetDir, "icon_${size}x$size.png").absolutePath
                )
            }
            exec {
                commandLine(
                    "sips",
                    "-z", (size * 2).toString(), (size * 2).toString(),
                    logoFile.absolutePath,
                    "--out",
                    File(iconsetDir, "icon_${size}x${size}@2x.png").absolutePath
                )
            }
        }

        exec {
            commandLine(
                "iconutil",
                "-c", "icns",
                iconsetDir.absolutePath,
                "-o", outputFile.absolutePath
            )
        }
    }
}

tasks.matching { it.name in setOf("packageDmg", "createDistributable") }.configureEach {
    dependsOn(generateMacIcon)
}

tasks.matching { it.name in setOf("packageMsi", "packageExe") }.configureEach {
    dependsOn(generateWindowsIcon)
}

compose.desktop {
    application {
        mainClass = "nihongo.tools.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Exe)
            packageName = "Nihongo Tools"
            packageVersion = appVersion
            description = "Desktop utilities for studying Japanese"
            vendor = "Nihongo Tools"
            modules(
                "java.base",
                "java.desktop",
                "java.logging",
                "java.management",
                "java.naming",
                "java.net.http",
                "java.prefs",
                "java.scripting",
                "java.security.jgss",
                "java.security.sasl",
                "java.sql",
                "java.transaction.xa",
                "java.xml",
                "java.xml.crypto",
                "jdk.unsupported"
            )

            windows {
                iconFile.set(windowsIconRegularFile)
                menu = true
                shortcut = true
                dirChooser = true
            }

            macOS {
                iconFile.set(macIconRegularFile)
                dockName = "Nihongo Tools"
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}
