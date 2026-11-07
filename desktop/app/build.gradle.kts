import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    implementation(project(":desktop:receiver"))
    implementation(project(":desktop:vjoy"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.zxing.core)
}

compose.desktop {
    application {
        mainClass = "app.volapex.desktop.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Msi)
            packageName = "Volapex"
            packageVersion = "0.1.0"
            description = "Volante virtual com o celular"
            vendor = "Volapex"
            // O instalador do vJoy (MIT, assinado por Justin Shafer) vai junto, em resources/windows.
            appResourcesRootDir.set(project.layout.projectDirectory.dir("resources"))
            includeAllModules = true
            windows {
                iconFile.set(project.layout.projectDirectory.file("icons/volapex.ico"))
                menuGroup = "Volapex"
                shortcut = true
                dirChooser = true
                // Fixo para sempre: permite que versões novas substituam as antigas.
                upgradeUuid = "e335b202-e430-4e1f-8440-a477b45b3703"
            }
        }
    }
}
