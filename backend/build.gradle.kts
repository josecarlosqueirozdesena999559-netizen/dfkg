plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.10"
    application
}

kotlin {
    jvmToolchain(21)
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin", "shared", "modules")
    }
}

group = "com.decisoes"
version = "0.0.1"

dependencies {
    // Ktor Server Core
    implementation("io.ktor:ktor-server-core-jvm:3.1.0")
    implementation("io.ktor:ktor-server-netty-jvm:3.1.0")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.1.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:3.1.0")
    implementation("io.ktor:ktor-server-cors-jvm:3.1.0")
    implementation("io.ktor:ktor-server-status-pages-jvm:3.1.0")
    implementation("io.ktor:ktor-server-auth-jvm:3.1.0")

    // Database
    implementation("org.jetbrains.exposed:exposed-core:0.58.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.58.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.58.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.58.0")
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.postgresql:postgresql:42.7.5")
    implementation(libs.h2)

    // Flyway Migrations
    implementation("org.flywaydb:flyway-core:10.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.10.0")
    // Firebase Admin SDK
    implementation("com.google.firebase:firebase-admin:9.4.3")

    // Logback logging
    implementation("ch.qos.logback:logback-classic:1.5.16")

    // Testing
    testImplementation("io.ktor:ktor-server-test-host-jvm:3.1.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:2.2.10")
}

application {
    mainClass.set("com.decisoes.app.ApplicationKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}
