/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.startup.component;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.processor.NotificationMappingProcessor;
import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.rest.proxy.ProxyManager;
import com.blackduck.integration.rest.proxy.ProxyInfo;

@Component
@Order(40)
public class ConfigurationLogger extends StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationLogger.class);

    private final ProxyManager proxyManager;
    private final AlertProperties alertProperties;
    private final NotificationMappingProcessor notificationMappingProcessor;

    @Autowired
    public ConfigurationLogger(ProxyManager proxyManager, AlertProperties alertProperties, NotificationMappingProcessor notificationMappingProcessor) {
        this.proxyManager = proxyManager;
        this.alertProperties = alertProperties;
        this.notificationMappingProcessor = notificationMappingProcessor;
    }

    @Override
    protected void initialize() {
        ProxyInfo proxyInfo = proxyManager.createProxyInfo();
        Optional<String> proxyHost = proxyInfo.getHost();
        Optional<String> proxyPort = Optional.of(proxyInfo.getPort()).map(Object::toString);
        Optional<String> proxyUsername = proxyInfo.getUsername();
        Optional<String> proxyPassword = proxyInfo.getMaskedPassword();

        boolean authenticatedProxy = StringUtils.isNotBlank(proxyPassword.orElse(""));

        logger.info("----------------------------------------");
        logger.info("Alert Configuration: ");
        logger.info("Alert Server URL:                 {}", alertProperties.getServerURL());
        logger.info("Logging level:                    {}", alertProperties.getLoggingLevel().orElse(""));
        logger.info("Alert Proxy Host:                 {}", proxyHost.orElse(""));
        logger.info("Alert Proxy Port:                 {}", proxyPort.orElse(""));
        logger.info("Alert Proxy Authenticated:        {}", authenticatedProxy);
        logger.info("Alert Proxy User:                 {}", proxyUsername.orElse(""));
        logger.info("Notification Mapping Batch Limit: {}", notificationMappingProcessor.getNotificationMappingBatchLimit());
        logger.info("");
        logger.info("----------------------------------------");
    }

}
