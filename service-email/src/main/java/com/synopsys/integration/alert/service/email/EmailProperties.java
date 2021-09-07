/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import java.util.Properties;

import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

// TODO create a factory for this class
public class EmailProperties {
    // property keys

    private final Properties javamailProperties;

    public EmailProperties(FieldUtility fieldUtility) {
        if (fieldUtility == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        javamailProperties = javamailPropertiesFactory.createJavaMailProperties(fieldUtility);
    }

    public EmailProperties(ConfigurationModel emailGlobalConfigurationModel) {
        this(new FieldUtility(emailGlobalConfigurationModel.getCopyOfKeyToFieldMap()));
    }

    public EmailProperties(EmailGlobalConfigModel globalConfiguration) {
        if (globalConfiguration == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }

        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        javamailProperties = javamailPropertiesFactory.createJavaMailProperties(globalConfiguration);
    }

    public EmailProperties(Properties javamailProperties) {
        this.javamailProperties = javamailProperties;
    }

    public Properties getJavamailProperties() {
        return javamailProperties;
    }

    public String getJavamailOption(EmailPropertyKeys key) {
        return getJavamailOption(key.getPropertyKey());
    }

    public String getJavamailOption(String key) {
        return javamailProperties.getProperty(key);
    }

}
