package net.mangolise.gamesdk.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class Log {
    private static final Map<Class<?>, Logger> loggers = new ConcurrentHashMap<>();

    public static Logger logger() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        StackTraceElement caller = stackTraceElements[2];
        Class<?> clazz;
        try {
            clazz = Log.class.getClassLoader().loadClass(caller.getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        // note that these lines cannot be put into a computeIfAbsent call because the lambda would be invoked in the context of the caller
        Logger logger = loggers.get(clazz);
        if (logger != null) {
            return logger;
        }
        logger = LoggerFactory.getLogger(clazz);
        loggers.put(clazz, logger);
        return logger;
    }
}
