/*
 * api-environment
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.api.environment;

import java.util.List;
import java.util.Optional;
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
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final List<EnvironmentVariableHandler<?>> environmentVariableHandlers;

    @Autowired
    public EnvironmentVariableProcessor(List<EnvironmentVariableHandler<?>> environmentVariableHandlers) {
        this.environmentVariableHandlers = environmentVariableHandlers;
    }

    public void updateConfigurations() {
        logger.info("** {} **", LINE_DIVIDER);
        logger.info("Initializing system configurations from environment variables...");
        logger.info("Begin handling environment variables...");
        for (EnvironmentVariableHandler<?> handler : environmentVariableHandlers) {
            logger.info(LINE_DIVIDER);
            logger.info("Handler name: {}", handler.getName());
            logger.info(LINE_DIVIDER);
            logVariableNames(handler.getVariableNames());
            EnvironmentProcessingResult result = handler.updateFromEnvironment();
            logConfiguration(result);
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

    private void logConfiguration(EnvironmentProcessingResult configurationProperties) {
        if (configurationProperties.hasValues()) {
            List<String> sortedVariableNames = configurationProperties.getVariableNames().stream()
                .sorted()
                .collect(Collectors.toList());
            logger.info(TWO_SPACE_INDENT);
            logger.info("{}### Environment Variables Used to Configure System ### ", TWO_SPACE_INDENT);
            for (String variableName : sortedVariableNames) {
                Optional<String> variableValue = configurationProperties.getVariableValue(variableName);
                variableValue.ifPresent(value -> logger.info("{}{} = {}", FOUR_SPACE_INDENT, variableName, value));
            }
            logger.info(TWO_SPACE_INDENT);
        }
    }
}
