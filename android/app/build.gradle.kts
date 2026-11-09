import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Assinatura de release: os dados ficam num arquivo fora do repositório, nunca no git.
// Caminho: propriedade -Pvwr.signing=..., variável VWR_SIGNING_PROPERTIES, ou a pasta padrão do mantenedor.
// Sem o arquivo, o build de release sai sem assinatura (útil para quem contribui e para a CI).
val signingFile = (providers.gradleProperty("vwr.signing").orNull
    ?: System.getenv("VWR_SIGNING_PROPERTIES")
    ?: "${System.getProperty("user.home")}/VirtualWheelRacing-assinatura/keystore.properties")
    .let(::file)
val signingProps = Properties().apply { if (signingFile.exists()) signingFile.inputStream().use { load(it) } }

android {
    namespace = "app.virtualwheelracing"
    compileSdk = 36

    defaultConfig {
        applicationId = "app.virtualwheelracing"
        minSdk = 29
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1"
    }

    signingConfigs {
        if (signingProps.isNotEmpty()) {
            create("release") {
                storeFile = file(signingProps.getProperty("storeFile"))
                storePassword = signingProps.getProperty("storePassword")
                keyAlias = signingProps.getProperty("keyAlias")
                keyPassword = signingProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            signingConfigs.findByName("release")?.let { signingConfig = it }
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":android:engine"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.play.services.code.scanner)
}
