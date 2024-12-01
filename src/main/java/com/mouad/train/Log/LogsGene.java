package com.mouad.train.Log;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Service
public class LogsGene {

    public String generateLog(String logLevel, String loggerName, String message, String contextData) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String threadName = Thread.currentThread().getName();
        return String.format("%s [%s] %s %s: %s - %s",
                timestamp,
                logLevel,
                threadName,
                loggerName,
                message,
                contextData != null ? contextData : "N/A");
    }
}
