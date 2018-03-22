/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.alert.channel.email;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;

public class EmailProperties {
    // property keys
    public final static String EMAIL_SERVICE_DISPATCHER_NOTIFICATION_INTERVAL_KEY = "email.service.dispatcher.notification.interval";
    public final static String EMAIL_SERVICE_DISPATCHER_NOTIFICATION_DELAY_KEY = "email.service.dispatcher.notification.delay";
    public final static String EMAIL_TEMPLATE_DIRECTORY = "hub.email.template.directory";
    public final static String EMAIL_LOGO_IMAGE = "logo.image";

    // common javamail properties
    public static final String JAVAMAIL_HOST_KEY = "mail.smtp.host";
    public static final String JAVAMAIL_PORT_KEY = "mail.smtp.port";
    public static final String JAVAMAIL_AUTH_KEY = "mail.smtp.auth";
    public static final String JAVAMAIL_USER_KEY = "mail.smtp.user";
    public static final String JAVAMAIL_CONNECTION_TIMEOUT_KEY = "mail.smtp.connectiontimeout";
    public static final String JAVAMAIL_TIMEOUT_KEY = "mail.smtp.timeout";
    public static final String JAVAMAIL_FROM_KEY = "mail.smtp.from";
    public static final String JAVAMAIL_LOCALHOST_KEY = "mail.smtp.localhost";
    public static final String JAVAMAIL_EHLO_KEY = "mail.smtp.ehlo";
    public static final String JAVAMAIL_DSN_NOTIFY_KEY = "mail.smtp.dsn.notify";
    public static final String JAVAMAIL_DSN_RET_KEY = "mail.smtp.dsn.ret";
    public static final String JAVAMAIL_ALLOW_8_BITMIME_KEY = "mail.smtp.allow8bitmime";
    public static final String JAVAMAIL_SEND_PARTIAL_KEY = "mail.smtp.sendpartial";

    // not a javamail property, but we are going to piggy-back to get teh smtp password
    public static final String JAVAMAIL_PASSWORD_KEY = "mail.smtp.password";

    // keys for alert descriptor data.
    public static final String TEMPLATE_KEY_HUB_SERVER_URL = "hub_server_url";
    public static final String TEMPLATE_KEY_SUBJECT_LINE = "subject_line";
    public static final String TEMPLATE_KEY_TOPIC = "topic";
    public static final String TEMPLATE_KEY_START_DATE = "startDate";
    public static final String TEMPLATE_KEY_END_DATE = "endDate";
    public static final String TEMPLATE_KEY_TOTAL_NOTIFICATIONS = "totalNotifications";
    public static final String TEMPLATE_KEY_TOTAL_POLICY_VIOLATIONS = "totalPolicyViolations";
    public static final String TEMPLATE_KEY_TOTAL_POLICY_OVERRIDES = "totalPolicyOverrides";
    public static final String TEMPLATE_KEY_TOTAL_VULNERABILITIES = "totalVulnerabilities";
    public static final String TEMPLATE_KEY_EMAIL_CATEGORY = "emailCategory";

    private String mailSmtpPassword;

    private final Map<String, String> javamailConfigProperties = new HashMap<>();

    public EmailProperties(final GlobalEmailConfigEntity emailConfigEntity) {
        if (emailConfigEntity == null) {
            throw new IllegalArgumentException("Could not find the global Email configuration");
        }
        updateFromConfig(emailConfigEntity);
    }

    public void updateFromConfig(final GlobalEmailConfigEntity emailConfigEntity) {
        addJavaMailOption(JAVAMAIL_HOST_KEY, emailConfigEntity.getMailSmtpHost());
        addJavaMailOption(JAVAMAIL_USER_KEY, emailConfigEntity.getMailSmtpUser());
        addJavaMailOption(JAVAMAIL_PORT_KEY, emailConfigEntity.getMailSmtpPort());
        addJavaMailOption(JAVAMAIL_CONNECTION_TIMEOUT_KEY, emailConfigEntity.getMailSmtpConnectionTimeout());
        addJavaMailOption(JAVAMAIL_TIMEOUT_KEY, emailConfigEntity.getMailSmtpTimeout());
        addJavaMailOption(JAVAMAIL_FROM_KEY, emailConfigEntity.getMailSmtpFrom());
        addJavaMailOption(JAVAMAIL_LOCALHOST_KEY, emailConfigEntity.getMailSmtpLocalhost());
        addJavaMailOption(JAVAMAIL_EHLO_KEY, emailConfigEntity.getMailSmtpEhlo());
        addJavaMailOption(JAVAMAIL_AUTH_KEY, emailConfigEntity.getMailSmtpAuth());
        addJavaMailOption(JAVAMAIL_DSN_NOTIFY_KEY, emailConfigEntity.getMailSmtpDnsNotify());
        addJavaMailOption(JAVAMAIL_DSN_RET_KEY, emailConfigEntity.getMailSmtpLocalhost());
        addJavaMailOption(JAVAMAIL_ALLOW_8_BITMIME_KEY, emailConfigEntity.getMailSmtpAllow8bitmime());
        addJavaMailOption(JAVAMAIL_SEND_PARTIAL_KEY, emailConfigEntity.getMailSmtpSendPartial());

        this.mailSmtpPassword = emailConfigEntity.getMailSmtpPassword();
        addJavaMailOption(JAVAMAIL_PASSWORD_KEY, this.mailSmtpPassword);
    }

    private void addJavaMailOption(final String key, final String value) {
        if (StringUtils.isNotEmpty(value)) {
            javamailConfigProperties.put(key, value);
        }
    }

    private void addJavaMailOption(final String key, final Boolean value) {
        if (value != null) {
            javamailConfigProperties.put(key, String.valueOf(value));
        }
    }

    private void addJavaMailOption(final String key, final Integer value) {
        if (value != null) {
            javamailConfigProperties.put(key, String.valueOf(value));
        }
    }

    public Map<String, String> getJavamailConfigProperties() {
        return javamailConfigProperties;
    }

    public String getJavamailOption(final String key) {
        return javamailConfigProperties.get(key);
    }

    public String getMailSmtpPassword() {
        return mailSmtpPassword;
    }

}
