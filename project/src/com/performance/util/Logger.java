package com.performance.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logger utility to print messages to the terminal.
 */
public class Logger {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        System.out.println(getTimestamp() + " [INFO] " + message);
    }

    public static void error(String message) {
        System.err.println(getTimestamp() + " [ERROR] " + message);
    }

    public static void warn(String message) {
        System.out.println(getTimestamp() + " [WARN] " + message);
    }

    private static String getTimestamp() {
        return LocalDateTime.now().format(formatter);
    }
}
