package com.mouad.train.Producer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducer {
    private final KafkaTemplate<String, String> kafkaTeplate ;

    public void sendMessage(String Message){
        log.info(String.format("A message sending to a the logTopic : << %s >> ", Message));
        kafkaTeplate.send("logsTopic",Message);
    }
}
