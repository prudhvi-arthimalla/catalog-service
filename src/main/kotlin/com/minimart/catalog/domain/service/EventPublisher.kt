package com.minimart.catalog.domain.service

import com.github.avrokotlin.avro4k.Avro
import com.minimart.catalog.domain.events.ProductCreated
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

@Service
class EventPublisher(private val kafkaTemplate: KafkaTemplate<String, Any>) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun publish(topic: String, key: String, event: ProductCreated) {
        val record: GenericRecord = Avro.default.toRecord(ProductCreated.serializer(), event)
        val producerRecord = ProducerRecord<String, Any>(topic, key, record)
        kafkaTemplate.send(producerRecord).whenComplete { _, ex ->
            if (null != ex) {
                log.error("Failed to publish to topic $topic, key $key, ${ex.message}", ex)
            }
            log.info("Published to topic $topic, key $key")
        }
    }
}
