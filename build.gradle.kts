import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor: String by project
val kotlinxSerialization: String by project

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

group = "com.github.discordPodcasts"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerialization") // Json serialization
    implementation("com.codahale:xsalsa20poly1305:0.11.0") // Encryption / Decryption
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}