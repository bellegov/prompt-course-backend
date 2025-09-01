package com.promptcourse.progressservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic cacheInvalidationTopic() {
        return TopicBuilder.name("cache-invalidation")
                .partitions(1) // Для разработки достаточно одной партиции
                .replicas(1)   // И одной реплики
                .build();
    }
}
