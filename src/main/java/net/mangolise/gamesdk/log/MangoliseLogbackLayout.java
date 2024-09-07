package net.mangolise.gamesdk.log;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.LayoutBase;
import org.slf4j.LoggerFactory;

public class MangoliseLogbackLayout extends LayoutBase<ILoggingEvent> {

    public static void init() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        LoggerContext loggerContext = rootLogger.getLoggerContext();
        loggerContext.reset();

        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        String classPathColor = "\u001B[38;5;244m";
        String messageColor = "\u001B[38;5;250m";
        String threadColor = "\u001B[38;5;41m";
        encoder.setPattern("%highlight(%-5level) " + threadColor + "[%thread] " + classPathColor + "%logger: " + messageColor + "%message%n");
        encoder.start();

        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(loggerContext);
        appender.setEncoder(encoder);
        appender.start();

        rootLogger.addAppender(appender);
    }

    public String doLayout(ILoggingEvent event) {
        StringBuffer sbuf = new StringBuffer(128);
        sbuf.append(event.getTimeStamp() - event.getLoggerContextVO().getBirthTime());
        sbuf.append(" ");
        sbuf.append(event.getLevel());
        sbuf.append(" [");
        sbuf.append(event.getThreadName());
        sbuf.append("] ");
        sbuf.append(event.getLoggerName());
        sbuf.append(" - ");
        sbuf.append(event.getFormattedMessage());
        sbuf.append(CoreConstants.LINE_SEPARATOR);

        return sbuf.toString();
    }
}
