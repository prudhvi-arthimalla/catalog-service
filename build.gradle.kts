plugins {
    kotlin("jvm") version "2.2.10"
	kotlin("plugin.spring") version "2.2.10"
    kotlin("plugin.serialization") version "2.2.10"
	id("org.springframework.boot") version "3.5.4"
	id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "6.25.0"
}

group = "com.minimart"
version = "0.0.1-SNAPSHOT"
description = "CRUD products and prices"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
    maven("https://packages.confluent.io/maven/")
}

dependencies {
    // Web/API
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    // Spring Kafka
    implementation("org.springframework.kafka:spring-kafka")
    // Data
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")
    // Serialization
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    // Reactive
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    // Avro4k for (de)serialization to GenericRecord
    implementation("com.github.avro-kotlin.avro4k:avro4k-core:2.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.1")
    // Apache kafka
    implementation("io.confluent:kafka-avro-serializer:7.7.0")
    // Kotlin runtime
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    // Observability
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Utilities
    implementation("org.apache.commons:commons-lang3:3.18.0")
    // API Docs
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.6.0")
    // Dev tools
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    // Testing - Integration
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:mongodb")
    // Testing - Runtime
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}
spotless {
    kotlin {
        ktfmt().dropboxStyle()
        target("src/**/*.kt", "src/**/*.kts")
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}
