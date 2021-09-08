/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
import java.util.function.BiConsumer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class JavamailPropertiesFactory {    
    public Properties createJavaMailProperties(EmailGlobalConfigModel globalConfiguration) {
        if (globalConfiguration == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        Properties javaMailProperties = new Properties();
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_FROM_KEY, globalConfiguration.getFrom());
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_HOST_KEY, globalConfiguration.getHost());
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_PORT_KEY, String.valueOf(globalConfiguration.getPort()));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_AUTH_KEY, String.valueOf(globalConfiguration.getAuth()));
        putIfNotEmpty(javaMailProperties::setProperty, JAVAMAIL_USER_KEY, globalConfiguration.getUsername());
        javaMailProperties.putAll(globalConfiguration.getAdditionalJavaMailProperties());

        return javaMailProperties;
    }

    private void putIfNotEmpty(BiConsumer<String, String> setter, EmailPropertyKeys emailPropertyKey, String value) {
        String keyString = emailPropertyKey.getPropertyKey();
        if (StringUtils.isNotEmpty(value)) {
            setter.accept(keyString, value);
        }
    }

}
