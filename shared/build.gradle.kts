plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.skie)
}

kotlin {

    // Target declarations - add or remove as needed below. These define
    // which platforms this KMP module supports.
    // See: https://kotlinlang.org/docs/multiplatform-discover-project.html#targets
    androidLibrary {
        namespace = "com.talkeys.shared"
        compileSdk = 35
        minSdk = 24

        withHostTestBuilder {
        }

        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    // For iOS targets, this is also where you should
    // configure native binary output. For more information, see:
    // https://kotlinlang.org/docs/multiplatform-build-native-binaries.html#build-xcframeworks

    // A step-by-step guide on how to include this library in an XCode
    // project can be found here:
    // https://developer.android.com/kotlin/multiplatform/migrate
    val xcfName = "sharedKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
            export(libs.androidx.lifecycle.viewmodel)
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
            export(libs.androidx.lifecycle.viewmodel)
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
            export(libs.androidx.lifecycle.viewmodel)
        }
    }

    // Source set declarations.
    // Declaring a target automatically creates a source set with the same name. By default, the
    // Kotlin Gradle Plugin creates additional source sets that depend on each other, since it is
    // common to share sources between related targets.
    // See: https://kotlinlang.org/docs/multiplatform-hierarchy.html
    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.kotlin.stdlib)
                
                // Coroutines
                implementation(libs.kotlinx.coroutines.core)
                
                // Serialization
                implementation(libs.kotlinx.serialization.json)
                
                // Date and Time
                implementation(libs.kotlinx.datetime)
                
                // Networking - Ktor
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.logging)
                
                // Database - SQLDelight
                implementation(libs.sqldelight.runtime)
                implementation(libs.sqldelight.coroutines.extensions)
                
                // Dependency Injection - Koin
                implementation(libs.koin.core)
                
                // Logging
                implementation(libs.kermit)

                // AndroidX Lifecycle ViewModel (KMP) — api so iOS Swift sees the ViewModel type
                api(libs.androidx.lifecycle.viewmodel)
            }
        }

        commonTest {
            dependencies {
                implementation(libs.kotlin.test)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.ktor.client.mock)
            }
        }

        androidMain {
            dependencies {
                // Android-specific implementations
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.ktor.client.android)
                implementation(libs.sqldelight.android.driver)
                implementation(libs.koin.android)
                
                // Authentication
                implementation(libs.google.play.services.auth)
                implementation(libs.androidx.datastore.preferences)
            }
        }

        getByName("androidDeviceTest") {
            dependencies {
                implementation(libs.androidx.runner)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
            }
        }

        iosMain {
            dependencies {
                // iOS-specific implementations
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
            }
        }
    }
}

// SQLDelight Configuration
sqldelight {
    databases {
        create("TalkeysDatabase") {
            packageName.set("com.talkeys.shared.database")
        }
    }
}

// XCFramework task
tasks.register("assembleXCFramework") {
    dependsOn(
        "linkReleaseFrameworkIosArm64",
        "linkReleaseFrameworkIosX64",
        "linkReleaseFrameworkIosSimulatorArm64"
    )
    
    doLast {
        val frameworkName = "sharedKit"
        val buildDir = layout.buildDirectory.asFile.get()
        val xcframeworkDir = File(buildDir, "XCFrameworks/release")
        val xcframeworkPath = File(xcframeworkDir, "${frameworkName}.xcframework")
        
        // Clean up existing XCFramework if it exists
        if (xcframeworkPath.exists()) {
            xcframeworkPath.deleteRecursively()
        }
        xcframeworkDir.mkdirs()
        
        // Create fat simulator framework first
        val simulatorFrameworkDir = File(buildDir, "bin/simulator/releaseFramework")
        simulatorFrameworkDir.mkdirs()
        
        // Copy iosX64 framework as base
        val x64FrameworkPath = File(buildDir, "bin/iosX64/releaseFramework/${frameworkName}.framework")
        val simFrameworkPath = File(simulatorFrameworkDir, "${frameworkName}.framework")
        x64FrameworkPath.copyRecursively(simFrameworkPath, overwrite = true)
        
        // Use lipo to create fat binary with both simulator architectures
        project.exec {
            commandLine(
                "lipo",
                "${buildDir}/bin/iosX64/releaseFramework/${frameworkName}.framework/${frameworkName}",
                "${buildDir}/bin/iosSimulatorArm64/releaseFramework/${frameworkName}.framework/${frameworkName}",
                "-create", "-output",
                "${simFrameworkPath}/${frameworkName}"
            )
        }
        
        // Now create XCFramework with device and fat simulator framework
        project.exec {
            commandLine(
                "xcodebuild", "-create-xcframework",
                "-framework", "${buildDir}/bin/iosArm64/releaseFramework/${frameworkName}.framework",
                "-framework", "${simFrameworkPath}",
                "-output", xcframeworkPath.absolutePath
            )
        }
        
        println("XCFramework created at: ${xcframeworkPath.absolutePath}")
    }
}
