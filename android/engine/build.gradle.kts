plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "app.virtualwheelracing.engine"
    compileSdk = 36

    defaultConfig {
        minSdk = 29
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(project(":shared:domain"))
    api(project(":shared:protocol"))

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${libs.versions.kotlin.get()}")
    testImplementation("junit:junit:4.13.2")
}
