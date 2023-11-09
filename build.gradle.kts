import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor: String by project
val kotlinxCoroutines: String by project
val kotlinxSerialization: String by project

plugins {
    kotlin("jvm") version "1.8.21"
    kotlin("plugin.serialization") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
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
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", kotlinxCoroutines)
    implementation("io.ktor:ktor-client-core:$ktor")
    implementation("io.ktor:ktor-client-cio:$ktor")
    implementation("io.ktor:ktor-client-websockets:$ktor")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$kotlinxSerialization") // Json serialization
    implementation("com.codahale:xsalsa20poly1305:0.11.0") // Encryption / Decryption

    compileOnly("ch.qos.logback", "logback-classic", "1.4.8")

    implementation("bot.myra", "Diskord", "2.2.8-testing3")
    implementation(kotlin("reflect"))
    implementation("org.reflections:reflections:0.10.2")
    implementation("ch.qos.logback", "logback-classic", "1.4.8")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

tasks.withType<Jar> {
    manifest { attributes["Main-Class"] = "com.github.discordPodcasts.wrapperJvm.BotKt" }
}