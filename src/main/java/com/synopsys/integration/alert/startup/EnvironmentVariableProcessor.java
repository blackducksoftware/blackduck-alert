package com.synopsys.integration.alert.startup;

import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.environment.EnvironmentVariableConfigurationHandler;

@Component
public class EnvironmentVariableProcessor {
    private static final String LINE_DIVIDER = "---------------------------------";
    private Logger logger = LoggerFactory.getLogger(getClass());
    private List<EnvironmentVariableConfigurationHandler> handlerList;

    @Autowired
    public EnvironmentVariableProcessor(List<EnvironmentVariableConfigurationHandler> handlerList) {
        this.handlerList = handlerList;
    }

    public void updateConfigurations() {
        logger.info("** {} **", LINE_DIVIDER);
        logger.info("Initializing system configurations from environment variables...");
        for (EnvironmentVariableConfigurationHandler handler : handlerList) {
            logger.info(LINE_DIVIDER);
            logger.info("Handler name: {}", handler.getName());
            logger.info(LINE_DIVIDER);
            logVariableNames(handler.getVariableNames());
            Properties updatedConfiguration = handler.updateFromEnvironment();
            logConfiguration(updatedConfiguration);
        }
    }

    private void logVariableNames(Set<String> names) {
        logger.info("  ### Environment Variables ### ");
        for (String name : names) {
            logger.info("    {}", name);
        }
    }

    private void logConfiguration(Properties configurationProperties) {
        if (!configurationProperties.isEmpty()) {
            logger.info("  ");
            logger.info("  ### Configuration ### ");
            configurationProperties.forEach(this::logField);
            logger.info("  ");
        }
    }

    private void logField(Object key, Object value) {
        logger.info(String.format("    {} = {}", key, value));
    }
}
