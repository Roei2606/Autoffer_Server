plugins {
    kotlin("jvm") version "1.9.0"
    kotlin("plugin.spring") version "1.9.0"
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "org.socialnetwork"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

//dependencies {
//    implementation("org.springframework.boot:spring-boot-starter-webflux")
//    implementation("org.springframework.boot:spring-boot-starter-rsocket")
//    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
//    implementation("org.jetbrains.kotlin:kotlin-reflect")
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
//    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
//    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
//    implementation("io.projectreactor.netty:reactor-netty-http")
//
//    //implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.7.0")
//
//
//    implementation(platform("io.projectreactor:reactor-bom:2020.0.2"))
//    implementation("io.projectreactor:reactor-core")
//    implementation("io.rsocket:rsocket-core:1.1.4")
//    implementation("io.rsocket:rsocket-transport-netty:1.1.4")
//    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.16.1")
//    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
//
//
//    testImplementation("org.springframework.boot:spring-boot-starter-test")
//    testImplementation("io.projectreactor:reactor-test")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
//
//    developmentOnly("org.springframework.boot:spring-boot-devtools")
//
//    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
//}

dependencies {
    // WebFlux - לתמיכה באפליקציה ריאקטיבית עם Spring Webflux
    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // RSocket - עבור RSocket Server
    implementation("org.springframework.boot:spring-boot-starter-rsocket")

    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

    // Netty HTTP - חובה לתמיכה ב-Websocket עם RSocket
    implementation("io.projectreactor.netty:reactor-netty-http")

    // Jackson Kotlin - סריאליזציה ודה-סריאליזציה של JSON
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Kotlin Reflect (עבור תמיכה ברפלקשן של Kotlin)
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    // Kotlin Standard Library
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // בדיקות Unit test ו-Integration test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")

    //PDF
    implementation("org.apache.pdfbox:pdfbox:2.0.30")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType <Test> {
    useJUnitPlatform()
}
