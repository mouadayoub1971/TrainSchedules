package com.mouad.frontend.Models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class LogEntry {
    private final StringProperty timestamp;
    private final StringProperty logLevel;
    private final StringProperty threadName;
    private final StringProperty loggerName;
    private final StringProperty message;
    private final StringProperty contextData;

    public LogEntry(String timestamp, String logLevel, String threadName,
                    String loggerName, String message, String contextData) {
        this.timestamp = new SimpleStringProperty(timestamp);
        this.logLevel = new SimpleStringProperty(logLevel);
        this.threadName = new SimpleStringProperty(threadName);
        this.loggerName = new SimpleStringProperty(loggerName);
        this.message = new SimpleStringProperty(message);
        this.contextData = new SimpleStringProperty(contextData);
    }

    // Getters for JavaFX TableView
    public StringProperty timestampProperty() { return timestamp; }
    public StringProperty logLevelProperty() { return logLevel; }
    public StringProperty threadNameProperty() { return threadName; }
    public StringProperty loggerNameProperty() { return loggerName; }
    public StringProperty messageProperty() { return message; }
    public StringProperty contextDataProperty() { return contextData; }

    // Standard getters
    public String getTimestamp() { return timestamp.get(); }
    public String getLogLevel() { return logLevel.get(); }
    public String getThreadName() { return threadName.get(); }
    public String getLoggerName() { return loggerName.get(); }
    public String getMessage() { return message.get(); }
    public String getContextData() { return contextData.get(); }
}