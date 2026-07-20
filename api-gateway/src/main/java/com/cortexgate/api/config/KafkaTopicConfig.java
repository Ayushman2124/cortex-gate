package com.cortexgate.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic chatRequestsTopic() {
        return TopicBuilder.name("chat-requests")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic aiResponsesTopic() {
        return TopicBuilder.name("ai-responses")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
