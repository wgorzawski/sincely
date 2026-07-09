plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.kotlinCompose) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.sqldelight) apply false
}

fun androidSdkHome(): String =
    System.getenv("ANDROID_HOME")
        ?: System.getenv("ANDROID_SDK_ROOT")
        ?: throw GradleException("ANDROID_HOME (or ANDROID_SDK_ROOT) is not set")

fun adbPath(): String = "${androidSdkHome()}/platform-tools/adb"
fun emulatorPath(): String = "${androidSdkHome()}/emulator/emulator"

/** Name of the AVD started by `startEmulatorIfNeeded` / `runOnEmulator`. */
val sincelyAvdName = "sincely"

tasks.register("buildApk") {
    group = "sincely"
    description = "Builds the debug APK (shortcut for :androidApp:assembleDebug)"
    dependsOn(":androidApp:assembleDebug")
    doLast {
        println("APK: androidApp/build/outputs/apk/debug/androidApp-debug.apk")
    }
}

val startEmulatorIfNeeded by tasks.registering {
    group = "sincely"
    description = "Starts the '$sincelyAvdName' AVD if no device/emulator is already " +
        "connected via adb, and waits until it has finished booting"
    doLast {
        val adb = adbPath()

        fun adbDevicesOutput(): String {
            val out = java.io.ByteArrayOutputStream()
            exec {
                commandLine(adb, "devices")
                standardOutput = out
            }
            return out.toString()
        }

        val alreadyRunning = adbDevicesOutput().lineSequence()
            .drop(1)
            .any { it.trim().endsWith("\tdevice") }

        if (alreadyRunning) {
            println("A device/emulator is already connected, skipping emulator start.")
            return@doLast
        }

        println("No device/emulator connected — starting AVD '$sincelyAvdName'...")
        val logFile = layout.buildDirectory.file("emulator.log").get().asFile
        logFile.parentFile.mkdirs()
        ProcessBuilder(emulatorPath(), "-avd", sincelyAvdName, "-no-snapshot-save")
            .redirectOutput(ProcessBuilder.Redirect.to(logFile))
            .redirectErrorStream(true)
            .start()

        exec { commandLine(adb, "wait-for-device") }

        println("Waiting for the emulator to finish booting...")
        var booted = false
        var attempts = 0
        while (!booted && attempts < 180) {
            val out = java.io.ByteArrayOutputStream()
            exec {
                commandLine(adb, "shell", "getprop", "sys.boot_completed")
                standardOutput = out
                isIgnoreExitValue = true
            }
            booted = out.toString().trim() == "1"
            if (!booted) {
                Thread.sleep(1000)
                attempts++
            }
        }
        if (!booted) {
            throw GradleException(
                "Emulator '$sincelyAvdName' did not finish booting within 3 minutes " +
                    "(see ${logFile.path})"
            )
        }
        println("Emulator booted.")
    }
}

// installDebug needs a connected device at execution time, so make sure the
// emulator-start task always runs (and finishes) before it.
gradle.projectsEvaluated {
    project(":androidApp").tasks.named("installDebug") {
        dependsOn(startEmulatorIfNeeded)
    }
}

tasks.register("runOnEmulator") {
    group = "sincely"
    description = "Starts the '$sincelyAvdName' emulator if needed, then builds, installs, " +
        "and launches the app on it"
    dependsOn(startEmulatorIfNeeded, ":androidApp:installDebug")
    doLast {
        exec {
            commandLine(
                adbPath(), "shell", "monkey",
                "-p", "app.sincely.android",
                "-c", "android.intent.category.LAUNCHER",
                "1",
            )
        }
    }
}
