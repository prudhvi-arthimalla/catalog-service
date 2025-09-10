package com.minimart.catalog

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.redpanda.RedpandaContainer
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class KafkaTestcontainersConfig {
    companion object {
        @Container
        @JvmStatic
        val redpanda: RedpandaContainer =
            RedpandaContainer(
                    // Any recent Redpanda works; this one has Schema Registry enabled by default
                    DockerImageName.parse("docker.redpanda.com/redpandadata/redpanda:v23.3.12"))
                .withReuse(false)

        @JvmStatic
        @DynamicPropertySource
        fun kafkaProps(registry: DynamicPropertyRegistry) {
            // CHANGE MADE: Wire Spring Boot to the container endpoints
            registry.add("spring.kafka.bootstrap-servers") { redpanda.bootstrapServers }
            // NOTE: method name may differ by version: schemaRegistryAddress OR schemaRegistryUrl
            registry.add("spring.kafka.properties.schema.registry.url") {
                redpanda.schemaRegistryAddress
            }
        }
    }
}
