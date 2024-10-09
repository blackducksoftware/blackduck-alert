/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.provider.blackduck.processor.message.service;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.blackduck.integration.rest.exception.IntegrationRestException;

@Component
public class BomComponent404Handler {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public void logIf404OrThrow(IntegrationRestException restException, String componentName, @Nullable String componentVersionName) throws IntegrationRestException {
        if (404 == restException.getHttpStatusCode()) {
            logger.debug("The BOM Component '{}[{}]' no longer exists", componentName, componentVersionName);
        } else {
            throw restException;
        }
    }

}
