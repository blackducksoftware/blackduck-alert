/*
 * test-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.test.common;

public enum TestPropertyKey {
    TEST_BLACKDUCK_PROVIDER_URL("blackduck.provider.url"),
    BLACKDUCK_URL("blackduck.url"),
    TEST_BLACKDUCK_PROVIDER_PORT("blackduck.provider.port"),
    TEST_BLACKDUCK_PROVIDER_USERNAME("blackduck.provider.username"),
    TEST_BLACKDUCK_PROVIDER_PASSWORD("blackduck.provider.password"),
    TEST_BLACKDUCK_PROVIDER_API_KEY("blackduck.provider.api.key"),
    BLACKDUCK_API_TOKEN("blackduck.api.token"),
    TEST_BLACKDUCK_PROVIDER_TRUST_HTTPS_CERT("blackduck.provider.trust.cert"),
    TEST_BLACKDUCK_PROVIDER_TIMEOUT("blackduck.provider.timeout"),
    TEST_BLACKDUCK_PROVIDER_ACTIVE_USER("blackduck.provider.active.user"),
    TEST_BLACKDUCK_PROVIDER_INACTIVE_USER("blackduck.provider.inactive.user"),
    TEST_BLACKDUCK_PROVIDER_PROJECT_NAME("blackduck.provider.project.name"),
    TEST_BLACKDUCK_PROVIDER_PROJECT_HREF("blackduck.provider.project.href"),
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
    TEST_ENCRYPTION_SALT("alert.encryption.global.salt"),

    TEST_PROXY_HOST("alert.proxy.host"),
    TEST_PROXY_PORT("alert.proxy.port"),
    TEST_PROXY_USERNAME("alert.proxy.username"),
    TEST_PROXY_PASSWORD("alert.proxy.password"),
    TEST_PROXY_NON_PROXY_HOSTS("alert.proxy.non.proxy.hosts"),

    TEST_JIRA_CLOUD_URL("alert.jira.cloud.url"),
    TEST_JIRA_CLOUD_USER_EMAIL("alert.jira.cloud.user.email"),
    TEST_JIRA_CLOUD_API_TOKEN("alert.jira.cloud.api.token"),
    TEST_JIRA_CLOUD_DISABLE_PLUGIN_CHECK("alert.jira.cloud.disable.plugin.check"),
    TEST_JIRA_CLOUD_ADD_COMMENTS("alert.jira.cloud.add.comments"),
    TEST_JIRA_CLOUD_ISSUE_CREATOR("alert.jira.cloud.issue.creator"),
    TEST_JIRA_CLOUD_PROJECT_NAME("alert.jira.cloud.project.name"),
    TEST_JIRA_CLOUD_ISSUE_TYPE("alert.jira.cloud.issue.type"),
    TEST_JIRA_CLOUD_RESOLVE_TRANSITION("alert.jira.cloud.resolve.transition"),
    TEST_JIRA_CLOUD_REOPEN_TRANSITION("alert.jira.cloud.reopen.transition"),

    TEST_JIRA_SERVER_URL("alert.jira.server.url"),
    TEST_JIRA_SERVER_USERNAME("alert.jira.server.username"),
    TEST_JIRA_SERVER_PASSWORD("alert.jira.server.password"),
    TEST_JIRA_SERVER_DISABLE_PLUGIN_CHECK("alert.jira.server.disable.plugin.check"),
    TEST_JIRA_SERVER_ADD_COMMENTS("alert.jira.server.add.comments"),
    TEST_JIRA_SERVER_ISSUE_CREATOR("alert.jira.server.issue.creator"),
    TEST_JIRA_SERVER_PROJECT_NAME("alert.jira.server.project.name"),
    TEST_JIRA_SERVER_ISSUE_TYPE("alert.jira.server.issue.type"),
    TEST_JIRA_SERVER_RESOLVE_TRANSITION("alert.jira.server.resolve.transition"),
    TEST_JIRA_SERVER_REOPEN_TRANSITION("alert.jira.server.reopen.transition");

    private final String propertyKey;

    TestPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
