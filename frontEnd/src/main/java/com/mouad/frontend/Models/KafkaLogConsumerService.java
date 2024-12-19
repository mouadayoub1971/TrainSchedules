package com.mouad.frontend.Controllers;


import com.mouad.frontend.Models.LogEntry;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KafkaLogConsumerService {
    private final ObservableList<LogEntry> logEntries;
    private final String topicName;
    private final ExecutorService executorService;
    private volatile boolean isRunning = false;

    public KafkaLogConsumerService(ObservableList<LogEntry> logEntries, String topicName) {
        this.logEntries = logEntries;
        this.topicName = topicName;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void startConsuming() {
        if (isRunning) return;

        isRunning = true;
        executorService.submit(this::consumeKafkaLogs);
    }

    private void consumeKafkaLogs() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "firstGroup");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        props.put("enable.auto.commit", "false");    // Disable auto offset commit

        // Optional: Add unique group.id to reset consumer group
        props.put("group.id", "unique-group-" + System.currentTimeMillis());

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(Collections.singletonList(topicName));

            while (isRunning) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    // Assuming the log message is in the format:
                    // "timestamp [logLevel] threadName loggerName: message - contextData"
                    String[] parts = record.value().split(" ", 6);
                    if (parts.length >= 6) {
                        final LogEntry logEntry = new LogEntry(
                                parts[0] + " " + parts[1],  // timestamp
                                parts[2].replace("[", "").replace("]", ""),  // logLevel
                                parts[3],  // threadName
                                parts[4],  // loggerName
                                parts[5].split(" - ")[0],  // message
                                parts.length > 5 ? parts[5].split(" - ")[1] : "N/A"  // contextData
                        );

                        Platform.runLater(() -> {
                            // Limit the number of log entries to prevent memory issues
                            if (logEntries.size() > 1000) {
                                logEntries.remove(0);
                            }
                            logEntries.add(logEntry);
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopConsuming() {
        isRunning = false;
        executorService.shutdown();
    }
}
