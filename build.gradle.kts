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
    maven(url = "https://systems.myra.bot/releases/") {
        credentials {
            username = property("REPO_NAME")?.toString()
            password = property("REPO_SECRET")?.toString()
        }
    }
}

dependencies {
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerialization") // Json serialization
    implementation("com.codahale:xsalsa20poly1305:0.11.0") // Encryption / Decryption

    testImplementation("bot.myra", "Diskord", "2.81")
    testImplementation(kotlin("reflect"))
    testImplementation("org.reflections:reflections:0.10.2")
    testImplementation("bot.myra:kommons:1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}