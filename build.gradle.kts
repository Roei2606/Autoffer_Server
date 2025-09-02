plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
    application
}

group = "org.socialnetwork"
version = "0.0.1-SNAPSHOT"

java { toolchain { languageVersion = JavaLanguageVersion.of(17) } }

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-rsocket")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("io.projectreactor.netty:reactor-netty-http")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("com.github.librepdf:openpdf:1.3.39")

    implementation("org.apache.pdfbox:pdfbox:2.0.30")

    implementation(project(":AutofferModelsRequests"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xjsr305=strict"
    }
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}
tasks.withType<Test> { useJUnitPlatform() }

application {
    mainClass.set("org.socialnetwork.messagingserver.MessagingServerApplicationKt")
}

tasks.register<JavaExec>("rsocketClient") {
    group = "verification"
    description = "Invoke RSocket endpoints (parse/preview/create) via WebSocket"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("org.socialnetwork.messagingserver.tools.RsocketWsTestKt")
    // אם תרצה לראות סטאקטרייסים מלאים:
    jvmArgs("-Dorg.slf4j.simpleLogger.defaultLogLevel=info")
}

