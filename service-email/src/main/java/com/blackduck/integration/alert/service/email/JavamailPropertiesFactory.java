/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.service.email;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.blackduck.integration.alert.service.email.model.EmailGlobalConfigModel;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;

@Component
public class JavamailPropertiesFactory {
    public Properties createJavaMailProperties(EmailGlobalConfigModel globalConfiguration) {
        if (globalConfiguration == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        Properties javaMailProperties = new Properties();
        javaMailProperties.setProperty(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), globalConfiguration.getSmtpFrom());
        javaMailProperties.setProperty(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), globalConfiguration.getSmtpHost());

        globalConfiguration.getSmtpPort()
            .map(String::valueOf)
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), value));

        globalConfiguration.getSmtpAuth()
            .map(String::valueOf)
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), value));

        globalConfiguration.getSmtpUsername()
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), value));

        globalConfiguration.getAdditionalJavaMailProperties().ifPresent(javaMailProperties::putAll);

        return javaMailProperties;
    }

    @Deprecated(forRemoval = true)
    public Properties createJavaMailProperties(FieldUtility fieldUtility) {
        if (fieldUtility == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        Properties javaMailProperties = new Properties();
        for (EmailPropertyKeys emailPropertyKey : EmailPropertyKeys.values()) {
            String key = emailPropertyKey.getPropertyKey();
            String value = fieldUtility.getStringOrEmpty(key);
            if (StringUtils.isNotBlank(value)) {
                javaMailProperties.setProperty(key, value);
            }
        }

        return javaMailProperties;
    }

}
