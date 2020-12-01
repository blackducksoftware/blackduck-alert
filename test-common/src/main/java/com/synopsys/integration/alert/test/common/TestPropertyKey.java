/**
 * test-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.test.common;

public enum TestPropertyKey {
    TEST_BLACKDUCK_PROVIDER_URL("blackduck.provider.url"),
    TEST_BLACKDUCK_PROVIDER_PORT("blackduck.provider.port"),
    TEST_BLACKDUCK_PROVIDER_USERNAME("blackduck.provider.username"),
    TEST_BLACKDUCK_PROVIDER_PASSWORD("blackduck.provider.password"),
    TEST_BLACKDUCK_PROVIDER_API_KEY("blackduck.provider.api.key"),
    TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT("blackduck.provider.trust.cert"),
    TEST_BLACKDUCK_PROVIDER_TIMEOUT("blackduck.provider.timeout"),
    TEST_BLACKDUCK_PROVIDER_ACTIVE_USER("blackduck.provider.active.user"),
    TEST_BLACKDUCK_PROVIDER_INACTIVE_USER("blackduck.provider.inactive.user"),
    TEST_BLACKDUCK_PROVIDER_PROJECT_NAME("blackduck.provider.project.name"),
    TEST_BLACKDUCK_PROVIDER_PROJECT_VERSION("blackduck.provider.project.version"),

    TEST_CRON("alert.tasks.cron"),
    TEST_DAILY_DIGEST("alert.digest.daily.cron"),
    TEST_REALTIME_DIGEST("alert.digest.realtime.cron"),

    TEST_SLACK_CHANNEL_NAME("slack.channel.name"),
    TEST_SLACK_USERNAME("slack.username"),
    TEST_SLACK_WEBHOOK("slack.web.hook"),

    TEST_MSTEAMS_WEBHOOK("msteams.web.hook"),

    TEST_EMAIL_RECIPIENT("mail.recipient"),
    TEST_EMAIL_SMTP_HOST("mail.smtp.host"),
    TEST_EMAIL_SMTP_FROM("mail.smtp.from"),
    TEST_EMAIL_SMTP_USER("mail.smtp.user"),
    TEST_EMAIL_SMTP_PASSWORD("mail.smtp.password"),
    TEST_EMAIL_SMTP_AUTH("mail.smtp.auth"),
    TEST_EMAIL_SMTP_EHLO("mail.smtp.ehlo"),
    TEST_EMAIL_SMTP_PORT("mail.smtp.port"),
    TEST_EMAIL_LOGO("logo.image"),
    TEST_ENCRYPTION_PASSWORD("alert.encryption.password"),
    TEST_ENCRYPTION_SALT("alert.encryption.global.salt");

    private final String propertyKey;

    TestPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
