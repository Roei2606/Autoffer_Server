plugins {
    kotlin("jvm") version "1.9.0"
    `java-library`
}

group = "org.socialnetwork"
version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

dependencies {
    api("com.fasterxml.jackson.core:jackson-annotations:2.17.2")
    compileOnly("org.springframework.data:spring-data-mongodb:4.3.0")
    compileOnly("org.springframework.data:spring-data-commons:3.3.0")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "17"
}
