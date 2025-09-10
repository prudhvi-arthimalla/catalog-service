package com.minimart.catalog.domain.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.TopicBuilder
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
@EnableKafka
class KafkaConfig(
    @param:Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
    @param:Value("\${spring.kafka.properties.schema.registry.url}")
    private val schemaRegistryUrl: String,
    @param:Value("\${kafka.topics.productRegistered}") private val productRegisteredTopic: String
) {
    // dev-only topic auto-creation remove later if you manage topics in kubernetes
    @Bean
    fun productRegistered(): NewTopic =
        TopicBuilder.name(productRegisteredTopic).partitions(3).replicas(1).build()

    // ProducerFactory for Confluent Avro serializer
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val props =
            mapOf(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to
                    io.confluent.kafka.serializers.KafkaAvroSerializer::class.java,
                "schema.registry.url" to schemaRegistryUrl)
        return DefaultKafkaProducerFactory(props)
    }

    @Bean fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())
}
