package com.synopsys.integration.alert.performance.utility;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.synopsys.integration.log.IntLogger;

public class PerformanceLoggingUtility {
    IntLogger logger;
    DateTimeFormatter dateTimeFormatter;

    public PerformanceLoggingUtility(IntLogger intLogger, DateTimeFormatter dateTimeFormatter) {
        this.logger = intLogger;
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        logger.info(String.format(messageFormat, durationFormatted));
        logger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }
}
