package com.mouad.train.Consumer;


import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class ConfigConsumerFromSpark {
    @Bean
    public NewTopic nouveauTopic(){
        return TopicBuilder
                .name("spark-out-put")
                .build();
    }
}
