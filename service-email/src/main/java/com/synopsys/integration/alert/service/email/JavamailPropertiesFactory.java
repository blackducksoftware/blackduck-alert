/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_AUTH_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_FROM_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_HOST_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_PORT_KEY;
import static com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys.JAVAMAIL_USER_KEY;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class JavamailPropertiesFactory {    
    public Properties createJavaMailProperties(EmailGlobalConfigModel globalConfiguration) {
        if (globalConfiguration == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        Properties javaMailProperties = new Properties();
        globalConfiguration.getSmtpFrom()
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(JAVAMAIL_FROM_KEY.getPropertyKey(), value));

        globalConfiguration.getSmtpHost()
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(JAVAMAIL_HOST_KEY.getPropertyKey(), value));

        globalConfiguration.getSmtpPort()
            .map(String::valueOf)
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(JAVAMAIL_PORT_KEY.getPropertyKey(), value));

        globalConfiguration.getSmtpAuth()
            .map(String::valueOf)
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(JAVAMAIL_AUTH_KEY.getPropertyKey(), value));

        globalConfiguration.getSmtpUsername()
            .filter(StringUtils::isNotBlank)
            .ifPresent(value -> javaMailProperties.setProperty(JAVAMAIL_USER_KEY.getPropertyKey(), value));

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
