package mikeshafter.iciwi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * A flexible logging system that creates human and machine-readable log files.
 * Log entries are stored as JSON objects which makes them easily parseable by code
 * and allows for simple expansion of log entry fields in the future.
 */
public class IcLogger {
private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
private final String logFilePath;
private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
private final Iciwi plugin = Iciwi.getPlugin(Iciwi.class);

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
public IcLogger () {
    this.logFilePath = "iciwi.log";
    initLogFile();
}

/**
 * Initialize the log file
 */
private void initLogFile () {
    File file = new File(plugin.getDataFolder(), logFilePath);
    Logger logger = plugin.getLogger();
    if (!file.exists()) {
        logger.log(Level.INFO, file.getParentFile().mkdirs() ? "[Iciwi] Logger file created!" : "[Iciwi] Logger file already exists, initialising...");
        plugin.saveResource(logFilePath, false);
    }
}

/**
 * Log a message with the specified level
 *
 * @param level The severity level of the log
 * @param message The message to log
 */
public void log (LogLevel level, String message) {
    logWithData(level, message, null);
}

/**
 * Log a message with additional data fields
 *
 * @param level The severity level of the log
 * @param message The message to log
 * @param data Additional data to include in the log entry
 */
public void logWithData (LogLevel level, String message, Map<String, String> data) {
    JsonObject logEntry = new JsonObject();
    logEntry.addProperty("timestamp", LocalDateTime.now().format(dateFormatter));
    logEntry.addProperty("level", level.toString());
    logEntry.addProperty("message", message);

    if (data != null && !data.isEmpty()) {
        JsonObject dataObj = new JsonObject();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            dataObj.add(entry.getKey(), convertToJsonElement(entry.getValue()));
        }
        logEntry.add("data", dataObj);
    }
    writeToLogFile(logEntry);
}

/**
 * Convert any object to a JsonElement that can be included in the log entry
 * Handles complex types including arrays with map-like objects
 *
 * @param value The object to convert
 * @return JsonElement representation of the object
 */
private JsonElement convertToJsonElement (Object value) {
    if (value == null) return null;

    if (!(value instanceof String || value instanceof Character || value instanceof Boolean || value instanceof Number))
        return gson.toJsonTree(value.toString());
    else
        return gson.toJsonTree(value);
}

/**
 * Convenience method for INFO level logs
 */
public void info (String message) {
    log(LogLevel.INFO, message);
}

/**
 * Convenience method for INFO level logs with additional data
 */
public void info (String message, Map<String, String> data) {
    logWithData(LogLevel.INFO, message, data);
}

/**
 * Convenience method for WARNING level logs
 */
public void warning (String message) {
    log(LogLevel.WARNING, message);
}

/**
 * Convenience method for WARNING level logs with additional data
 */
public void warning (String message, Map<String, String> data) {
    logWithData(LogLevel.WARNING, message, data);
}

/**
 * Convenience method for ERROR level logs
 */
public void error (String message) {
    log(LogLevel.ERROR, message);
}

/**
 * Convenience method for ERROR level logs with additional data
 */
public void error (String message, Map<String, String> data) {
    logWithData(LogLevel.ERROR, message, data);
}

/**
 * Convenience method for DEBUG level logs
 */
public void debug (String message) {
    log(LogLevel.DEBUG, message);
}

/**
 * Convenience method for DEBUG level logs with additional data
 */
public void debug (String message, Map<String, String> data) {
    logWithData(LogLevel.DEBUG, message, data);
}

/**
 * Write a log entry to the log file
 *
 * @param logEntry The JSON object representing the log entry
 */
private void writeToLogFile (JsonObject logEntry) {
    try (FileWriter writer = new FileWriter(logFilePath, true)) {
        writer.write(gson.toJson(logEntry));
        writer.write("\n"); // Add newline for readability
    } catch (IOException e) {
        System.err.println("Failed to write to log file: " + e.getMessage());
    }
}
}
