package com.myproject.crispysystem.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerUtil {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    private static String getContext() {
        // Get the current thread's stack trace
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // The calling method is at index 3 (this method -> logX -> caller)
        if (stackTrace.length > 3) {
            StackTraceElement caller = stackTrace[3];
            return String.format("[%s.%s]", caller.getClassName(), caller.getMethodName());
        }
        return "[Unknown Context]";
    }

    public static void logInfo(String message) {
        logger.info("{} {}", getContext(), message);
    }

    public static void logError(String message, Throwable e) {
        logger.error("{} {}", getContext(), message, e);
    }

    public static void logWarn(String message) {
        logger.warn("{} {}", getContext(), message);
    }

    public static void logDebug(String message) {
        logger.debug("{} {}", getContext(), message);
    }
}

