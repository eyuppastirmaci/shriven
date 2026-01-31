package com.eyuppastirmaci.shriven.backend.config

import com.eyuppastirmaci.shriven.backend.analytics.dto.ClickEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.kafka.autoconfigure.KafkaProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.kafka.support.serializer.JacksonJsonSerializer

@Configuration
open class KafkaConfig {

    @Bean
    open fun producerFactory(kafkaProperties: KafkaProperties): ProducerFactory<String, ClickEvent> {
        val props = HashMap(kafkaProperties.buildProducerProperties())

        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JacksonJsonSerializer::class.java

        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, ClickEvent>): KafkaTemplate<String, ClickEvent> {
        return KafkaTemplate(producerFactory)
    }

    @Bean
    open fun consumerFactory(kafkaProperties: KafkaProperties): ConsumerFactory<String, ClickEvent> {
        val props = HashMap(kafkaProperties.buildConsumerProperties())

        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java

        // [2] Use JacksonJsonDeserializer instead of JsonDeserializer
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JacksonJsonDeserializer::class.java

        // [3] Set trusted packages (using string literal to avoid deprecation warnings on the constant)
        props["spring.json.trusted.packages"] = "*"

        return DefaultKafkaConsumerFactory(props)
    }

    @Bean
    open fun kafkaListenerContainerFactory(consumerFactory: ConsumerFactory<String, ClickEvent>): ConcurrentKafkaListenerContainerFactory<String, ClickEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, ClickEvent>()

        factory.setConsumerFactory(consumerFactory)
        factory.setBatchListener(true)
        factory.containerProperties.ackMode = ContainerProperties.AckMode.BATCH

        return factory
    }
}