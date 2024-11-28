package com.mouad.train.Producer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ConfigProducer {
    @Bean
    public NewTopic newTopic(){
        return TopicBuilder
                .name("logsTopic").
                build();

    }
}
