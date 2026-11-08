import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
    api(project(":shared:domain"))
    implementation(libs.jna)

    testImplementation(testFixtures(project(":shared:domain")))
    testImplementation(kotlin("test"))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    // O teste de contrato com o vJoy real só roda com: gradlew :desktop:vjoy:test -Pvwr.vjoy=true
    systemProperty("vwr.vjoy", providers.gradleProperty("vwr.vjoy").getOrElse("false"))
}
