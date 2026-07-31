plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "dev.frammenti.fuckumeter"

version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()

    testLogging {
        events("passed", "skipped", "failed")
    }
}

dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.callId)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.hsts)
    implementation(ktorLibs.server.metrics.micrometer)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.statusPages)
    implementation(libs.h2database.h2)
    implementation(libs.logback.classic)
    implementation(libs.micrometer.registryPrometheus)
    implementation(libs.postgresql)
    implementation(libs.hikari)
    implementation(libs.kotliquery)
    implementation(libs.flyway)
    implementation(libs.flyway.postgresql)
    implementation(libs.google.auth)

    testImplementation(ktorLibs.server.testHost)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform)
}
