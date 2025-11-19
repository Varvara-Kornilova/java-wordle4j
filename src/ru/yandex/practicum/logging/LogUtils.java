package ru.yandex.practicum.logging;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LogUtils() {}

    public static void logInfrastructureError(PrintWriter logWriter, String message) {
        log(logWriter, "[INFRA ERROR]", message);
    }

    public static void logGameError(PrintWriter logWriter, String message) {
        log(logWriter, "[GAME ERROR]", message);
    }

    public static void logInfo(PrintWriter logWriter, String message) {
        log(logWriter, "[INFO]", message);
    }

    private static void log(PrintWriter logWriter, String level, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        logWriter.println("[" + timestamp + "] " + level + " " + message);
        logWriter.flush();
    }
}
