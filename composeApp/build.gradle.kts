import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization").version("2.1.20")
}

kotlin {
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)


            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.java)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.content.negotiation)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            //implementation("io.ktor:ktor-client-logging-jvm:2.3.12")
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.rogr.swishpayoutclient.MainKt"

        nativeDistributions {

            modules(
                "java.base",
                "java.desktop",
                "java.logging",
                "java.naming",
                "java.xml",
                "java.net.http",   // <-- REQUIRED for Ktor Java engine
                "jdk.crypto.ec"    // <-- TLS with EC certs
            )
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AstonMartinPays"
            packageVersion = "1.0.0"
            macOS {
                iconFile.set(project.layout.projectDirectory.file("icons/macos.icns"))
            }
            windows {
                iconFile.set(project.layout.projectDirectory.file("icons/icon.png"))
            }
            linux {
                iconFile.set(project.layout.projectDirectory.file("icons/icon.png"))
            }

        }
    }
}
