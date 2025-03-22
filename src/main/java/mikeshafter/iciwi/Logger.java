package mikeshafter.iciwi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * A flexible logging system that creates human and machine-readable log files.
 * Log entries are stored as JSON objects which makes them easily parseable by code
 * and allows for simple expansion of log entry fields in the future.
 */
public class Logger {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String logFilePath;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * LogLevel enum to categorize log messages
     */
    public enum LogLevel {
        INFO,
        WARNING,
        ERROR,
        DEBUG
    }
    
    /**
     * Faster constructor
     */
    public Logger() {
        this.logFilePath = "iciwi.log";
        initLogFile();
    }
    
    /**
     * Initialize the log file with headers if it doesn't exist
     */
    private void initLogFile() {
        Path path = Paths.get(logFilePath);
        
        if (!Files.exists(path)) {
            try {
                // Create directory if it doesn't exist
                Files.createDirectories(path.getParent());
                
                // Create the log file with a header
                String header = "# ICIWI Log File\n";
                header += "# Format: JSON (one object per line)\n";
                header += "# Created: " + LocalDateTime.now().format(dateFormatter) + "\n";
                
                Files.write(path, header.getBytes(), StandardOpenOption.CREATE);
            } catch (IOException e) {
                System.err.println("Failed to initialize log file: " + e.getMessage());
            }
        }
    }
    
    /**
     * Log a message with the specified level
     * 
     * @param level The severity level of the log
     * @param message The message to log
     */
    public void log(LogLevel level, String message) {
        logWithData(level, message, null);
    }
    
    /**
     * Log a message with additional data fields
     * 
     * @param level The severity level of the log
     * @param message The message to log
     * @param data Additional data to include in the log entry
     */
    public void logWithData(LogLevel level, String message, Map<String, Object> data) {
        LocalDateTime now = LocalDateTime.now();
        
        // Create the base log entry
        JsonObject logEntry = new JsonObject();
        logEntry.addProperty("timestamp", now.format(dateFormatter));
        logEntry.addProperty("level", level.toString());
        logEntry.addProperty("message", message);
        
        // Add any additional data fields
        if (data != null && !data.isEmpty()) {
            JsonObject dataObj = new JsonObject();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (entry.getValue() instanceof String) {
                    dataObj.addProperty(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof Number) {
                    dataObj.addProperty(entry.getKey(), (Number) entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    dataObj.addProperty(entry.getKey(), (Boolean) entry.getValue());
                } else {
                    dataObj.addProperty(entry.getKey(), gson.toJson(entry.getValue()));
                }
            }
            logEntry.add("data", dataObj);
        }
        
        writeToLogFile(logEntry);
    }
    
    /**
     * Convenience method for INFO level logs
     */
    public void info(String message) {
        log(LogLevel.INFO, message);
    }
    
    /**
     * Convenience method for INFO level logs with additional data
     */
    public void info(String message, Map<String, Object> data) {
        logWithData(LogLevel.INFO, message, data);
    }
    
    /**
     * Convenience method for WARNING level logs
     */
    public void warning(String message) {
        log(LogLevel.WARNING, message);
    }
    
    /**
     * Convenience method for WARNING level logs with additional data
     */
    public void warning(String message, Map<String, Object> data) {
        logWithData(LogLevel.WARNING, message, data);
    }
    
    /**
     * Convenience method for ERROR level logs
     */
    public void error(String message) {
        log(LogLevel.ERROR, message);
    }
    
    /**
     * Convenience method for ERROR level logs with additional data
     */
    public void error(String message, Map<String, Object> data) {
        logWithData(LogLevel.ERROR, message, data);
    }
    
    /**
     * Convenience method for DEBUG level logs
     */
    public void debug(String message) {
        log(LogLevel.DEBUG, message);
    }
    
    /**
     * Convenience method for DEBUG level logs with additional data
     */
    public void debug(String message, Map<String, Object> data) {
        logWithData(LogLevel.DEBUG, message, data);
    }
    
    /**
     * Write a log entry to the log file
     * 
     * @param logEntry The JSON object representing the log entry
     */
    private void writeToLogFile(JsonObject logEntry) {
        try (FileWriter writer = new FileWriter(logFilePath, true)) {
            writer.write(gson.toJson(logEntry));
            writer.write("\n"); // Add newline for readability
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
}
