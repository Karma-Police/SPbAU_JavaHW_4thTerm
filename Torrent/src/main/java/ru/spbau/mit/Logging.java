package ru.spbau.mit;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by michael on 24.05.16.
 */
public class Logging {
    public static void setLevel(Level level) {
        Logger rootLog = Logger.getLogger("");
        rootLog.setLevel(level);
        rootLog.setFilter((logRecord) -> logRecord.getLoggerName().startsWith("ru.spbau"));
        rootLog.getHandlers()[0].setLevel(level);
        rootLog.getHandlers()[0].setFilter((logRecord) -> logRecord.getLoggerName().startsWith("ru.spbau"));
    }
}
