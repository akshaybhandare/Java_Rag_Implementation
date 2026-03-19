package org.simple.rag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple singleton logger for the RAG application.
 * 
 * Provides basic logging functionality with INFO, WARN, and ERROR levels.
 * All log messages are prefixed with timestamp and level.
 * 
 * Thread-safe singleton pattern implementation.
 */
public class Logger {

    private static final Logger instance = new Logger();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
        // Prevent instantiation
    }

    /**
     * Get the singleton instance of the logger.
     * 
     * @return The Logger instance
     */
    public static Logger getInstance() {
        return instance;
    }

    /**
     * Log an informational message.
     * 
     * @param message The message to log
     */
    public void info(String message) {
        log("INFO", message);
    }

    /**
     * Log a warning message.
     * 
     * @param message The message to log
     */
    public void warn(String message) {
        log("WARN", message);
    }

    /**
     * Log an error message.
     * 
     * @param message The message to log
     */
    public void error(String message) {
        log("ERROR", message, true);
    }

    /**
     * Log an error message with exception details.
     * 
     * @param message The message to log
     * @param throwable The exception to log
     */
    public void error(String message, Throwable throwable) {
        error(message);
        if (throwable != null) {
            throwable.printStackTrace(System.err);
        }
    }

    private void log(String level, String message) {
        log(level, message, false);
    }

    private void log(String level, String message, boolean isError) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String logMessage = String.format("[%s] [%s] %s", timestamp, level, message);

        if (isError) {
            System.err.println(logMessage);
        } else {
            System.out.println(logMessage);
        }
    }
}
