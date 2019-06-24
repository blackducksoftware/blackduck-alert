package com.synopsys.integration.alert.util;

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

    TEST_POLARIS_PROVIDER_URL("polaris.provider.url"),
    TEST_POLARIS_PROVIDER_ACCESS_TOKEN("polaris.provider.access.token"),
    TEST_POLARIS_PROVIDER_TIMEOUT("polaris.provider.timeout"),

    TEST_CRON("alert.tasks.cron"),
    TEST_DAILY_DIGEST("alert.digest.daily.cron"),
    TEST_REALTIME_DIGEST("alert.digest.realtime.cron"),

    TEST_SLACK_CHANNEL_NAME("slack.channel.name"),
    TEST_SLACK_USERNAME("slack.username"),
    TEST_SLACK_WEBHOOK("slack.web.hook"),

    TEST_EMAIL_RECIPIENT("mail.recipient"),
    TEST_EMAIL_SMTP_HOST("mail.smtp.host"),
    TEST_EMAIL_SMTP_FROM("mail.smtp.from"),
    TEST_EMAIL_SMTP_USER("mail.smtp.user"),
    TEST_EMAIL_SMTP_PASSWORD("mail.smtp.password"),
    TEST_EMAIL_SMTP_AUTH("mail.smtp.auth"),
    TEST_EMAIL_SMTP_EHLO("mail.smtp.ehlo"),
    TEST_EMAIL_SMTP_PORT("mail.smtp.port"),
    TEST_EMAIL_LOGO("logo.image");

    private final String propertyKey;

    TestPropertyKey(final String propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyKey() {
        return propertyKey;
    }
}
