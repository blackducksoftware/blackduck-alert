/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.environment;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentVariableProcessor {
    private static final String LINE_DIVIDER = "---------------------------------";
    private static final String TWO_SPACE_INDENT = "  ";
    private static final String FOUR_SPACE_INDENT = "    ";
    private Logger logger = LoggerFactory.getLogger(getClass());
    private final List<EnvironmentVariableHandlerFactory> factoryList;

    @Autowired
    public EnvironmentVariableProcessor(List<EnvironmentVariableHandlerFactory> factoryList) {
        this.factoryList = factoryList;
    }

    public void updateConfigurations() {
        logger.info("** {} **", LINE_DIVIDER);
        logger.info("Initializing system configurations from environment variables...");
        logger.info("Building environment variables handlers.");
        List<EnvironmentVariableHandler> handlerList = factoryList.stream()
            .map(EnvironmentVariableHandlerFactory::build)
            .collect(Collectors.toList());
        logger.info("Begin handling environment variables...");
        for (EnvironmentVariableHandler handler : handlerList) {
            logger.info(LINE_DIVIDER);
            logger.info("Handler name: {}", handler.getName());
            logger.info(LINE_DIVIDER);
            logVariableNames(handler.getVariableNames());
            Properties updatedConfiguration = handler.updateFromEnvironment();
            logConfiguration(updatedConfiguration);
        }
    }

    private void logVariableNames(Set<String> names) {
        logger.info("{}### Environment Variables ### ", TWO_SPACE_INDENT);
        List<String> sortedNames = names.stream()
            .map(String::trim)
            .sorted()
            .collect(Collectors.toList());

        for (String name : sortedNames) {
            logger.info("{}{}", FOUR_SPACE_INDENT, name);
        }
    }

    private void logConfiguration(Properties configurationProperties) {
        if (!configurationProperties.isEmpty()) {
            List<String> sortedPropertyNames = configurationProperties.entrySet().stream()
                .map(Map.Entry::getKey)
                .map(Object::toString)
                .sorted()
                .collect(Collectors.toList());
            logger.info(TWO_SPACE_INDENT);
            logger.info("{}### Environment Variables Used to Configure System ### ", TWO_SPACE_INDENT);
            for (String propertyName : sortedPropertyNames) {
                logger.info("{}{} = {}", FOUR_SPACE_INDENT, propertyName, configurationProperties.get(propertyName));
            }
            logger.info(TWO_SPACE_INDENT);
        }
    }
}
