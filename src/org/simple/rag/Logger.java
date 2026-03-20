package org.simple.rag;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final Logger instance = new Logger();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Logger() {
    }

    public static Logger getInstance() {
        return instance;
    }

    public void info(String message) {
        log("INFO", message);
    }

    public void warn(String message) {
        log("WARN", message);
    }

    public void error(String message) {
        log("ERROR", message, true);
    }

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
