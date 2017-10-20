/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.blackducksoftware.integration.hub.alert.channel.email.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class EmailProperties {
    // property keys
    public final static String EMAIL_FROM_ADDRESS_KEY = "email.from.address";

    public final static String EMAIL_REPLY_TO_ADDRESS_KEY = "email.reply.to.address";

    public final static String EMAIL_SERVICE_DISPATCHER_NOTIFICATION_INTERVAL_KEY = "email.service.dispatcher.notification.interval";

    public final static String EMAIL_SERVICE_DISPATCHER_NOTIFICATION_DELAY_KEY = "email.service.dispatcher.notification.delay";

    public final static String EMAIL_TEMPLATE_DIRECTORY = "hub.email.template.directory";

    // common javamail properties
    public static final String JAVAMAIL_HOST_KEY = "mail.smtp.host";

    public static final String JAVAMAIL_PORT_KEY = "mail.smtp.port";

    public static final String JAVAMAIL_AUTH_KEY = "mail.smtp.auth";

    public static final String JAVAMAIL_USER_KEY = "mail.smtp.user";

    // not a javamail property, but we are going to piggy-back on the
    // auto-parsing for javamail properties to get the password
    public static final String JAVAMAIL_PASSWORD_KEY = "mail.smtp.password";

    // keys for alert descriptor data.
    public static final String JAVAMAIL_CONFIG_PREFIX = "hub.email.javamail.config.";

    public static final String TEMPLATE_VARIABLE_PREFIX = "hub.email.template.variable.";

    public static final String NOTIFIER_PREFIX = "hub.email.service.notifier.";

    public static final String TRANSFORMER_CONTENT_ITEM_PREFIX = "hub.email.service.transformer.content.";

    public static final String OPT_OUT_PREFIX = "hub.email.user.preference.opt.out.";

    public static final String NOTIFIER_LAST_RUN_PREFIX = "hub.email.service.notifier.lastrun.";

    public static final String NOTIFIER_VARIABLE_PREFIX = "hub.email.notifier.variable.";

    public static final String ALERT_PREFIX = "hub.alert.";

    public static final String ALERT_SSL_KEYSTORE_PATH = "hub.alert.ssl.keyStorePath";

    public static final String ALERT_SSL_KEYSTORE_PASSWORD = "hub.alert.ssl.keyStorePassword";

    public static final String ALERT_SSL_KEY_PASSWORD = "hub.alert.ssl.keyPassword";

    public static final String ALERT_SSL_KEYSTORE_TYPE = "hub.alert.ssl.keyStoreType";

    public static final String TEMPLATE_KEY_HUB_SERVER_URL = "hub_server_url";

    public static final String TEMPLATE_KEY_SUBJECT_LINE = "subject_line";

    public static final String TEMPLATE_KEY_TOPICS_LIST = "topicsList";

    public static final String TEMPLATE_KEY_START_DATE = "startDate";

    public static final String TEMPLATE_KEY_END_DATE = "endDate";

    public static final String TEMPLATE_KEY_TOTAL_NOTIFICATIONS = "totalNotifications";

    public static final String TEMPLATE_KEY_TOTAL_POLICY_VIOLATIONS = "totalPolicyViolations";

    public static final String TEMPLATE_KEY_TOTAL_POLICY_OVERRIDES = "totalPolicyOverrides";

    public static final String TEMPLATE_KEY_TOTAL_VULNERABILITIES = "totalVulnerabilities";

    public static final String TEMPLATE_KEY_EMAIL_CATEGORY = "emailCategory";

    public static final String TEMPLATE_KEY_USER_FIRST_NAME = "user_first_name";

    public static final String TEMPLATE_KEY_USER_LAST_NAME = "user_last_name";

    private final Map<String, String> suppliedJavamailConfigProperties = new HashMap<>();

    private final Map<String, String> suppliedTemplateVariableProperties = new HashMap<>();

    private final List<String> notifierClassNames = new ArrayList<>();

    private final Map<String, String> optOutProperties = new HashMap<>();

    private final Map<String, String> notifierVariableProperties = new HashMap<>();

    private final Map<String, String> alertProperties = new HashMap<>();

    private final Properties appProperties;

    public EmailProperties(final Properties appProperties) {
        if (appProperties == null) {
            throw new IllegalArgumentException("appProperties argument cannot be null");
        }
        this.appProperties = appProperties;
        extractProperties(appProperties);
    }

    public EmailProperties(final Properties defaults, final Properties appProperties) {
        if (defaults == null) {
            throw new IllegalArgumentException("defaults argument cannot be null");
        }
        if (appProperties == null) {
            throw new IllegalArgumentException("appProperties argument cannot be null");
        }
        this.appProperties = new Properties();
        extractProperties(defaults);
        extractProperties(appProperties);
    }

    public void extractProperties(final Properties properties) {
        for (final Object obj : properties.keySet()) {
            if (obj instanceof String == false) {
                appProperties.put(obj, properties.get(obj));
            } else {
                final String key = (String) obj;
                final String value = properties.getProperty(key);
                if (StringUtils.isNotBlank(value)) {
                    appProperties.put(key, value);
                    if (key.startsWith(ALERT_PREFIX)) {
                        final String cleanedKey = key.replace(ALERT_PREFIX, "");
                        alertProperties.put(cleanedKey, value);
                    } else if (key.startsWith(JAVAMAIL_CONFIG_PREFIX)) {
                        final String cleanedKey = key.replace(JAVAMAIL_CONFIG_PREFIX, "");
                        suppliedJavamailConfigProperties.put(cleanedKey, value);
                    } else if (key.startsWith(TEMPLATE_VARIABLE_PREFIX)) {
                        final String cleanedKey = key.replace(TEMPLATE_VARIABLE_PREFIX, "");
                        suppliedTemplateVariableProperties.put(cleanedKey, value);
                    } else if (key.startsWith(NOTIFIER_PREFIX)) {
                        notifierClassNames.add(value);
                    } else if (key.startsWith(OPT_OUT_PREFIX)) {
                        final String cleanedKey = key.replace(OPT_OUT_PREFIX, "");
                        optOutProperties.put(cleanedKey, value);
                    } else if (key.startsWith(NOTIFIER_VARIABLE_PREFIX)) {
                        final String cleanedKey = key.replace(NOTIFIER_VARIABLE_PREFIX, "");
                        notifierVariableProperties.put(cleanedKey, value);
                    }
                }
            }
        }
    }

    public Map<String, String> getSuppliedJavamailConfigProperties() {
        return suppliedJavamailConfigProperties;
    }

    public Map<String, String> getSuppliedTemplateVariableProperties() {
        return suppliedTemplateVariableProperties;
    }

    public Map<String, String> getPropertiesForSession() {
        return getSuppliedJavamailConfigProperties();
    }

    public String getHost() {
        return getSuppliedJavamailConfigProperties().get(JAVAMAIL_HOST_KEY);
    }

    public int getPort() {
        return NumberUtils.toInt(getSuppliedJavamailConfigProperties().get(JAVAMAIL_PORT_KEY));
    }

    public boolean isAuth() {
        return Boolean.parseBoolean(getSuppliedJavamailConfigProperties().get(JAVAMAIL_AUTH_KEY));
    }

    public String getUsername() {
        return getSuppliedJavamailConfigProperties().get(JAVAMAIL_USER_KEY);
    }

    public String getPassword() {
        return getSuppliedJavamailConfigProperties().get(JAVAMAIL_PASSWORD_KEY);
    }

    public String getEmailFromAddress() {
        return appProperties.getProperty(EMAIL_FROM_ADDRESS_KEY);
    }

    public String getEmailReplyToAddress() {
        return appProperties.getProperty(EMAIL_REPLY_TO_ADDRESS_KEY);
    }

    public String getNotificationInterval() {
        return appProperties.getProperty(EMAIL_SERVICE_DISPATCHER_NOTIFICATION_INTERVAL_KEY);
    }

    public String getNotificationStartupDelay() {
        return appProperties.getProperty(EMAIL_SERVICE_DISPATCHER_NOTIFICATION_DELAY_KEY);
    }

    public String getEmailTemplateDirectory() {
        return appProperties.getProperty(EMAIL_TEMPLATE_DIRECTORY);
    }

    public List<String> getNotifierClassNames() {
        return notifierClassNames;
    }

    public Map<String, String> getOptOutProperties() {
        return optOutProperties;
    }

    public String getProperty(final String key) {
        return appProperties.getProperty(key);
    }

    public Map<String, String> getNotifierVariableProperties() {
        return notifierVariableProperties;
    }

    public Properties getAppProperties() {
        return appProperties;
    }

    public String getSSLKeyStorePath() {
        return appProperties.getProperty(ALERT_SSL_KEYSTORE_PATH);
    }

    public String getSSLKeyStorePassword() {
        return appProperties.getProperty(ALERT_SSL_KEYSTORE_PASSWORD);
    }

    public String getSSLKeyPassword() {
        return appProperties.getProperty(ALERT_SSL_KEY_PASSWORD);
    }

    public String getSSLKeyStoreType() {
        return appProperties.getProperty(ALERT_SSL_KEYSTORE_TYPE);
    }
}
