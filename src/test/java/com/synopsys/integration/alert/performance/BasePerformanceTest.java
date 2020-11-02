package com.synopsys.integration.alert.performance;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.performance.utility.AlertRequestUtility;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.Slf4jIntLogger;

@Tag(TestTags.DEFAULT_PERFORMANCE)
public abstract class BasePerformanceTest {
    public static final String ROLE_ALERT_ADMIN = "ALERT_ADMIN";
    protected final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(this.getClass()));

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private AlertRequestUtility alertRequestUtility;

    public abstract AlertRequestUtility createAlertRequestUtility();

    @BeforeEach
    public void setupTests() {
        alertRequestUtility = createAlertRequestUtility();
    }

    public void logTimeElapsedWithMessage(String messageFormat, LocalDateTime start, LocalDateTime end) {
        //TODO log timing to a file
        Duration duration = Duration.between(start, end);
        String durationFormatted = String.format("%sH:%sm:%ss", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart());
        intLogger.info(String.format(messageFormat, durationFormatted));
        intLogger.info(String.format("Current time %s.", dateTimeFormatter.format(end)));
    }

}
